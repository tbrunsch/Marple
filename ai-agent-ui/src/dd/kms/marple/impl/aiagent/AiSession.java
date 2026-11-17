package dd.kms.marple.impl.aiagent;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.aiagent.backend.*;
import dd.kms.marple.impl.aiagent.mcp.EvaluatorSettingsTools;
import dd.kms.marple.impl.aiagent.mcp.MarpleTools;
import dd.kms.marple.impl.aiagent.mcp.McpServer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;

/**
 * Owns the MCP server and the agent of one AI view. Both are started lazily on the first request.
 */
class AiSession
{
	private static final String	MCP_SERVER_NAME				= "marple";
	private static final int	MAX_TRANSCRIPT_SUMMARY_LENGTH	= 10000;

	private final AiAgentViewSettings	settings;
	private final AiAgentFactory		agentFactory;
	private final InspectionContext		context;
	private final PermissionHandler		permissionHandler;
	private final Object				thisValue;

	private final Object				lock						= new Object();
	@Nullable
	private McpServer					mcpServer;
	@Nullable
	private EvaluatorSettingsTools		evaluatorSettingsTools;
	@Nullable
	private volatile AiAgent			agent;
	@Nullable
	private volatile Object				resumeState;
	@Nullable
	private String						transcriptSummary;
	private AgentVariables				agentVariables;
	/**
	 * Incremented whenever another conversation starts (see {@link #reset()} and {@link #restore(Object, List, AgentVariables)}).
	 * It is only written on the event dispatch thread while holding the lock.
	 */
	private int							conversationId;
	private volatile boolean			closed;

	AiSession(AiAgentViewSettings settings, AiAgentFactory agentFactory, InspectionContext context, PermissionHandler permissionHandler, Object thisValue) {
		this.settings = settings;
		this.agentFactory = agentFactory;
		this.context = context;
		this.permissionHandler = permissionHandler;
		this.thisValue = thisValue;
		this.agentVariables = new AgentVariables(context.getEvaluator());
	}

	/**
	 * Restores a previous conversation. If the agent cannot resume the conversation, then the
	 * transcript is passed to the agent as context with the next request. The variables the agent
	 * has created in that conversation are attached again. Must be called on the event dispatch thread.
	 */
	void restore(@Nullable Object resumeState, List<TranscriptEntry> transcript, AgentVariables agentVariables) {
		synchronized (lock) {
			closeAgent();
			conversationId++;
			if (agentVariables != this.agentVariables) {
				this.agentVariables.deleteAll();
				this.agentVariables = agentVariables;
				agentVariables.attach();
			}
			this.resumeState = resumeState;
			this.transcriptSummary = resumeState == null && !transcript.isEmpty()
				? createTranscriptSummary(transcript)
				: null;
		}
	}

	/**
	 * Returns the ID of the current conversation, which has to be passed to {@link #sendRequest(int, String, String, AiAgentListener)}.
	 * Must be called on the event dispatch thread.
	 */
	int getConversationId() {
		return conversationId;
	}

	/**
	 * Sends a request to the agent. Must not be called on the event dispatch thread.
	 *
	 * @param conversationId	the ID of the conversation the request belongs to (see {@link #getConversationId()}),
	 *							determined when the user sends the request. If another conversation has been started
	 *							in the meantime, then the request is not sent.
	 * @param contextLine		describes the current "this"; it is prepended to the message
	 *
	 * @throws InterruptedIOException if another conversation has been started in the meantime
	 */
	String sendRequest(int conversationId, String message, String contextLine, AiAgentListener listener) throws IOException {
		AiAgent agent;
		String fullMessage;
		synchronized (lock) {
			if (conversationId != this.conversationId) {
				throw new InterruptedIOException("The conversation has been replaced");
			}
			agent = getOrCreateAgent();
			fullMessage = contextLine + "\n" + message;
			if (transcriptSummary != null) {
				fullMessage = transcriptSummary + "\n\n" + fullMessage;
				transcriptSummary = null;
			}
		}
		return agent.sendRequest(fullMessage, listener);
	}

	void cancelRequest() {
		AiAgent agent = this.agent;
		if (agent != null) {
			agent.cancelRequest();
		}
	}

	@Nullable
	Object getResumeState() {
		AiAgent agent = this.agent;
		if (agent != null) {
			Object agentResumeState = agent.getResumeState();
			if (agentResumeState != null) {
				return agentResumeState;
			}
		}
		return resumeState;
	}

	/**
	 * The variables the agent has created in the current conversation. Must be called on the event dispatch thread.
	 */
	AgentVariables getAgentVariables() {
		synchronized (lock) {
			return agentVariables;
		}
	}

	/**
	 * Starts a new conversation. The variables the agent has created are deleted. Must be called on the event
	 * dispatch thread.
	 */
	void reset() {
		synchronized (lock) {
			closeAgent();
			conversationId++;
			agentVariables.deleteAll();
			agentVariables = new AgentVariables(context.getEvaluator());
			resumeState = null;
			transcriptSummary = null;
		}
	}

	/**
	 * Closes the agent and stops the MCP server. Changes of the evaluator settings made by the agent
	 * are reverted. The variables the agent has created are detached: They are removed from the evaluator,
	 * but they remain part of the conversation (see {@link AgentVariables}). The session cannot be used anymore afterwards (see {@link #reset()} for starting
	 * a new conversation instead).
	 * <p>
	 * This method does not throw exceptions, is idempotent, and does not block for a long time, so it
	 * can be called on the event dispatch thread. It may be called while a request is running: The agent
	 * is closed before acquiring the lock, which makes the running request terminate with an exception.
	 */
	void close() {
		closed = true;
		AiAgent agent = this.agent;
		if (agent != null) {
			agent.close();
		}
		synchronized (lock) {
			stopMcpServer();
			AgentVariables agentVariables = this.agentVariables;
			EventDispatchThreadUtils.runNowOrLater(agentVariables::detach);
		}
	}

	private AiAgent getOrCreateAgent() throws IOException {
		if (closed) {
			throw new IOException("The AI session has been closed");
		}
		AiAgent agent = this.agent;
		if (agent != null) {
			return agent;
		}
		if (mcpServer == null) {
			evaluatorSettingsTools = new EvaluatorSettingsTools(context.getEvaluator());
			List<AiTool> tools = ImmutableList.<AiTool>builder()
				.addAll(new MarpleTools(context, thisValue, agentVariables).createTools())
				.addAll(evaluatorSettingsTools.createTools())
				.build();
			McpServer server = new McpServer(tools);
			server.start();
			mcpServer = server;
		}
		agent = agentFactory.createAgent(new Environment(mcpServer), resumeState);
		this.agent = agent;
		return agent;
	}

	private void closeAgent() {
		AiAgent agent = this.agent;
		if (agent != null) {
			Object agentResumeState = agent.getResumeState();
			if (agentResumeState != null) {
				resumeState = agentResumeState;
			}
			agent.close();
			this.agent = null;
		}
		// tools registered by the agent are not valid anymore => new MCP server for the next agent
		stopMcpServer();
	}

	/**
	 * Stops the MCP server and reverts the changes of the evaluator settings made by the agent.
	 */
	private void stopMcpServer() {
		if (mcpServer != null) {
			mcpServer.stop();
			mcpServer = null;
		}
		if (evaluatorSettingsTools != null) {
			evaluatorSettingsTools.revertChanges();
			evaluatorSettingsTools = null;
		}
	}

	private String createSystemPrompt() {
		String systemPrompt = SystemPrompt.TEXT;
		String addition = settings.getSystemPromptAddition();
		return addition == null || addition.trim().isEmpty()
			? systemPrompt
			: systemPrompt + "\n\n" + addition;
	}

	private static String createTranscriptSummary(List<TranscriptEntry> transcript) {
		StringBuilder summary = new StringBuilder();
		for (TranscriptEntry entry : transcript) {
			switch (entry.getRole()) {
				case USER:
					summary.append("User: ").append(entry.getText()).append("\n");
					break;
				case AGENT:
					summary.append("You: ").append(entry.getText()).append("\n");
					break;
				default:
					break;
			}
		}
		String text = summary.length() <= MAX_TRANSCRIPT_SUMMARY_LENGTH
			? summary.toString()
			: "..." + summary.substring(summary.length() - MAX_TRANSCRIPT_SUMMARY_LENGTH);
		return "[Previous conversation for context]\n" + text + "[End of previous conversation]";
	}

	private class Environment implements AiAgentEnvironment
	{
		private final McpServer	server;

		Environment(McpServer server) {
			this.server = server;
		}

		@Override
		public McpEndpoint getMcpEndpoint() {
			return server.getEndpoint();
		}

		@Override
		public String getMcpServerName() {
			return MCP_SERVER_NAME;
		}

		@Override
		public List<AiTool> getTools() {
			return ImmutableList.copyOf(server.getTools());
		}

		@Override
		public String getSystemPrompt() {
			return createSystemPrompt();
		}

		@Override
		public PermissionHandler getPermissionHandler() {
			return permissionHandler;
		}

		@Override
		public void registerAdditionalTool(AiTool tool) {
			server.addTool(tool);
		}
	}
}
