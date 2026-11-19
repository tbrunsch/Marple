package dd.kms.marple.impl.aiagent;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.aiagent.backend.*;
import dd.kms.marple.impl.aiagent.mcp.EvaluatorSettingsTools;
import dd.kms.marple.impl.aiagent.mcp.MarpleTools;
import dd.kms.marple.impl.aiagent.mcp.McpServer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * Owns the MCP server and the agent of one AI view. Both are created lazily when the first request is sent.<br>
 * <br>
 * Thread safety: All methods must be called on the event dispatch thread. Only the agent's
 * {@link AiAgent#sendRequest(String, AiAgentListener)} runs in another thread. That request belongs to the
 * agent it has been sent to: Starting another conversation closes that agent, which makes the request terminate.
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
	private AiAgent						agent;
	/**
	 * The resume state the next agent is created with. While an agent exists, its own resume state is authoritative.
	 */
	@Nullable
	private Object						initialResumeState;
	@Nullable
	private String						transcriptSummary;
	private AgentContext				agentContext;
	private boolean						closed;

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
	 * conversation is used again.
	 */
	void restore(@Nullable Object resumeState, List<TranscriptEntry> transcript, AgentContext agentContext) {
		closeAgent();
		this.agentContext = agentContext;
		this.initialResumeState = resumeState;
		this.transcriptSummary = resumeState == null && !transcript.isEmpty()
			? createTranscriptSummary(transcript)
			: null;
	}

	/**
	 * Returns the agent of the current conversation and creates it if necessary. This is cheap because
	 * agents start lazily on the first request (see {@link AiAgentFactory#createAgent(AiAgentEnvironment, Object)}).
	 * The request must be sent to the returned agent outside the event dispatch thread.
	 *
	 * @throws IOException if the session has been closed or the MCP server cannot be started
	 */
	AiAgent getOrCreateAgent() throws IOException {
		if (closed) {
			throw new IOException("The AI session has been closed");
		}
		if (agent != null) {
			return agent;
		}
		if (mcpServer == null) {
			List<AiTool> tools = ImmutableList.<AiTool>builder()
				.addAll(new MarpleTools(context, thisValue, agentContext).createTools())
				.addAll(new EvaluatorSettingsTools(agentContext.getEvaluator()).createTools())
				.build();
			mcpServer = McpServer.start(tools);
		}
		agent = agentFactory.createAgent(new Environment(mcpServer), initialResumeState);
		// from now on, the agent's resume state is authoritative
		initialResumeState = null;
		return agent;
	}

	/**
	 * Creates the message that has to be sent to the agent for the user's message. The first message after
	 * restoring a conversation that cannot be resumed contains the previous transcript.
	 *
	 * @param contextLine	describes the current "this"; it is prepended to the message
	 */
	String createFullMessage(String message, String contextLine) {
		String fullMessage = contextLine + "\n" + message;
		if (transcriptSummary != null) {
			fullMessage = transcriptSummary + "\n\n" + fullMessage;
			transcriptSummary = null;
		}
		return fullMessage;
	}

	void cancelRequest() {
		if (agent != null) {
			agent.cancelRequest();
		}
	}

	@Nullable
	Object getResumeState() {
		return agent != null ? agent.getResumeState() : initialResumeState;
	}

	/**
	 * The agent's evaluator of the current conversation
	 */
	AgentContext getAgentContext() {
		return agentContext;
	}

	/**
	 * Starts a new conversation with a new evaluator for the agent
	 */
	void reset() {
		closeAgent();
		agentContext = new AgentContext(context.getEvaluator());
		initialResumeState = null;
		transcriptSummary = null;
	}

	/**
	 * Closes the agent and stops the MCP server. The agent's evaluator remains part of the conversation
	 * (see {@link AgentContext}). The session cannot be used anymore afterwards (see {@link #reset()} for starting
	 * a new conversation instead).
	 * <p>
	 * This method does not throw exceptions, is idempotent, and does not block for a long time. It may be
	 * called while a request is running, which then terminates with an exception. Since the agent is discarded,
	 * {@link #getResumeState()} does not return its resume state afterwards: The resume state must be queried before.
	 */
	void close() {
		closed = true;
		closeAgent();
	}

	private void closeAgent() {
		if (agent != null) {
			agent.close();
			agent = null;
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
