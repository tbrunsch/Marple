package dd.kms.marple.impl.aiagent;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.aiagent.backend.*;
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

	private final AiAgentViewSettings		settings;
	private final InspectionContext		context;
	private final PermissionHandler		permissionHandler;
	private final Object				thisValue;

	private final Object				lock						= new Object();
	@Nullable
	private McpServer					mcpServer;
	@Nullable
	private volatile AiAgent			agent;
	@Nullable
	private volatile Object				resumeState;
	@Nullable
	private String						transcriptSummary;
	/**
	 * Incremented whenever another conversation starts (see {@link #reset()} and {@link #restore(Object, List)}).
	 * It is only written on the event dispatch thread while holding the lock.
	 */
	private int							conversationId;
	private volatile boolean			closed;

	AiSession(AiAgentViewSettings settings, InspectionContext context, PermissionHandler permissionHandler, Object thisValue) {
		this.settings = settings;
		this.context = context;
		this.permissionHandler = permissionHandler;
		this.thisValue = thisValue;
	}

	/**
	 * Restores a previous conversation. If the agent cannot resume the conversation, then the
	 * transcript is passed to the agent as context with the next request.
	 */
	void restore(@Nullable Object resumeState, List<TranscriptEntry> transcript) {
		synchronized (lock) {
			closeAgent();
			conversationId++;
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
	 * Starts a new conversation.
	 */
	void reset() {
		synchronized (lock) {
			closeAgent();
			conversationId++;
			resumeState = null;
			transcriptSummary = null;
		}
	}

	void close() {
		closed = true;
		AiAgent agent = this.agent;
		if (agent != null) {
			agent.close();
		}
		McpServer mcpServer;
		synchronized (lock) {
			mcpServer = this.mcpServer;
			this.mcpServer = null;
		}
		if (mcpServer != null) {
			mcpServer.stop();
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
		AiAgentFactory agentFactory = settings.getAgentFactory();
		if (agentFactory == null) {
			throw new IOException(settings.getUnavailableMessage());
		}
		if (mcpServer == null) {
			List<AiTool> tools = new MarpleTools(context, thisValue).createTools();
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
