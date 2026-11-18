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
 * Owns the MCP server and the agent of one AI view. Both are started lazily on the first request.<br>
 * <br>
 * Thread safety: The mutable state is guarded by {@code this}. The lock is never held while waiting for
 * the agent, so the request can be stopped and the session can be closed or reset while a request is running.
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

	@Nullable
	private McpServer					mcpServer;
	@Nullable
	private volatile AiAgent			agent;
	/**
	 * The resume state the next agent is created with. While an agent exists, its own resume state is authoritative.
	 */
	@Nullable
	private volatile Object				initialResumeState;
	@Nullable
	private String						transcriptSummary;
	private AgentContext				agentContext;
	/**
	 * Incremented whenever another conversation starts (see {@link #reset()} and {@link #restore(Object, List, AgentContext)}).
	 * It is only written on the event dispatch thread while holding the monitor of this session.
	 */
	private volatile int				conversationId;
	private volatile boolean			closed;

	AiSession(AiAgentViewSettings settings, AiAgentFactory agentFactory, InspectionContext context, PermissionHandler permissionHandler, Object thisValue) {
		this.settings = settings;
		this.agentFactory = agentFactory;
		this.context = context;
		this.permissionHandler = permissionHandler;
		this.thisValue = thisValue;
		this.agentContext = new AgentContext(context.getEvaluator());
	}

	/**
	 * Restores a previous conversation. If the agent cannot resume the conversation, then the
	 * transcript is passed to the agent as context with the next request. The agent's evaluator of that
	 * conversation is used again. Must be called on the event dispatch thread.
	 */
	synchronized void restore(@Nullable Object resumeState, List<TranscriptEntry> transcript, AgentContext agentContext) {
		closeAgent();
		conversationId++;
		this.agentContext = agentContext;
		this.initialResumeState = resumeState;
		this.transcriptSummary = resumeState == null && !transcript.isEmpty()
			? createTranscriptSummary(transcript)
			: null;
	}

	/**
	 * Returns the ID of the current conversation, which has to be passed to {@link #sendRequest(int, String, String, AiAgentListener)}.
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
		synchronized (this) {
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
		// read in the opposite order of getOrCreateAgent()'s writes: if the initial state has already been cleared, then the agent is visible
		Object initialResumeState = this.initialResumeState;
		AiAgent agent = this.agent;
		return agent != null ? agent.getResumeState() : initialResumeState;
	}

	/**
	 * The agent's evaluator of the current conversation. Must be called on the event dispatch thread.
	 */
	synchronized AgentContext getAgentContext() {
		return agentContext;
	}

	/**
	 * Starts a new conversation with a new evaluator for the agent. Must be called on the event dispatch thread.
	 */
	synchronized void reset() {
		closeAgent();
		conversationId++;
		agentContext = new AgentContext(context.getEvaluator());
		initialResumeState = null;
		transcriptSummary = null;
	}

	/**
	 * Closes the agent and stops the MCP server. The agent's evaluator remains part of the conversation
	 * (see {@link AgentContext}). The session cannot be used anymore afterwards (see {@link #reset()} for starting
	 * a new conversation instead).
	 * <p>
	 * This method does not throw exceptions, is idempotent, and does not block for a long time, so it
	 * can be called on the event dispatch thread. It may be called while a request is running: The agent
	 * is closed before acquiring the monitor of this session, which makes the running request terminate with an exception.
	 */
	void close() {
		closed = true;
		AiAgent agent = this.agent;
		if (agent != null) {
			agent.close();
		}
		synchronized (this) {
			stopMcpServer();
		}
	}

	/**
	 * Must be called while holding the monitor of this session.
	 */
	private AiAgent getOrCreateAgent() throws IOException {
		if (closed) {
			throw new IOException("The AI session has been closed");
		}
		AiAgent agent = this.agent;
		if (agent != null) {
			return agent;
		}
		if (mcpServer == null) {
			List<AiTool> tools = ImmutableList.<AiTool>builder()
				.addAll(new MarpleTools(context, thisValue, agentContext).createTools())
				.addAll(new EvaluatorSettingsTools(agentContext.getEvaluator()).createTools())
				.build();
			McpServer server = new McpServer(tools);
			server.start();
			mcpServer = server;
		}
		agent = agentFactory.createAgent(new Environment(mcpServer), initialResumeState);
		this.agent = agent;
		// from now on, the agent's resume state is authoritative; cleared after setting the agent (see getResumeState())
		initialResumeState = null;
		return agent;
	}

	private void closeAgent() {
		AiAgent agent = this.agent;
		if (agent != null) {
			agent.close();
			this.agent = null;
		}
		// tools registered by the agent are not valid anymore => new MCP server for the next agent
		stopMcpServer();
	}

	private void stopMcpServer() {
		if (mcpServer != null) {
			mcpServer.stop();
			mcpServer = null;
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
