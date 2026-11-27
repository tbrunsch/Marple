package dd.kms.marple.impl.aiagent.claude;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import dd.kms.marple.api.aiagent.backend.*;

import javax.annotation.Nullable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.*;

import static dd.kms.marple.impl.aiagent.JsonUtils.*;

/**
 * Runs one long-lived Claude Code process ({@code claude -p} with stream-json input and output)
 * per conversation. If the process terminates (e.g., because a request has been canceled), then
 * it is restarted with {@code --resume <session id>} on the next request.
 * <p>
 * Thread safety: All mutable state is guarded by {@code this}. The lock is never held while waiting for
 * Claude Code: {@link #sendRequest(String, AiAgentListener)} waits for the answer without holding the lock,
 * so {@link #cancelRequest()}, {@link #close()}, and {@link #getResumeState()} can be called while a request
 * is running. The only longer operation under the lock is starting the process (see {@link #beginRequest()}),
 * which typically takes milliseconds. This is accepted to keep the state handling simple.
 */
class ClaudeCodeAgent implements AiAgent
{
	private enum State
	{
		IDLE,
		RUNNING,
		/**
		 * The request has been canceled, but {@link #sendRequest(String, AiAgentListener)} has not returned yet.
		 */
		CANCELING,
		/**
		 * The agent has been closed. This state is final. A request that is still running when the
		 * agent is closed terminates with an exception.
		 */
		CLOSED
	}

	private static final String			PERMISSION_TOOL_NAME	= "approve_permission";
	/**
	 * Tools whose arguments contain a file path and changes that can be shown as preview
	 */
	private static final Set<String>	FILE_EDIT_TOOLS			= ImmutableSet.of("Edit", "Write", "MultiEdit", "NotebookEdit");
	private static final long			POLL_INTERVAL_MS		= 200;
	private static final int			MAX_SUMMARY_LENGTH		= 200;
	private static final Gson			GSON					= new GsonBuilder().disableHtmlEscaping().create();
	private static final Gson			PRETTY_GSON				= new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private final String				executable;
	private final Path					workingDirectory;
	/**
	 * Maps the available built-in tools to whether they require approval
	 */
	private final Map<String, Boolean>	tools;
	private final List<String>			additionalArguments;
	private final AiAgentEnvironment	environment;

	/**
	 * The session ID is chosen by us (via {@code --session-id}) such that we know it in advance.
	 * It is replaced if the process terminates before the session is known to exist (see
	 * {@link #onProcessTerminated(ClaudeCodeProcess)}).
	 */
	private String						sessionId;
	private boolean						sessionExists;
	/**
	 * The process for the next request or {@code null} if a new process has to be started. A process
	 * is removed from this field as soon as it is terminated (see {@link #terminateCurrentProcess(boolean)}),
	 * so it is never reused. A running request keeps its own reference and notices the termination via
	 * the end of the stream or {@link #checkNotAborted()}.
	 */
	@Nullable
	private ClaudeCodeProcess			currentProcess;
	private State						state					= State.IDLE;

	ClaudeCodeAgent(String executable, Path workingDirectory, Map<String, Boolean> tools, List<String> additionalArguments, AiAgentEnvironment environment, @Nullable Object resumeState) {
		String sessionIdToResume = resumeState instanceof ClaudeCodeResumeState
			? ((ClaudeCodeResumeState) resumeState).getSessionId()
			: null;
		this.executable = executable;
		this.workingDirectory = workingDirectory;
		this.tools = tools;
		this.additionalArguments = additionalArguments;
		this.environment = environment;
		this.sessionId = sessionIdToResume != null ? sessionIdToResume : UUID.randomUUID().toString();
		this.sessionExists = sessionIdToResume != null;
		environment.registerAdditionalTool(createPermissionTool());
	}

	@Override
	public String sendRequest(String message, AiAgentListener listener) throws IOException {
		ClaudeCodeProcess process = beginRequest();
		try {
			writeUserMessage(process, message);
			return awaitResult(process, listener);
		} finally {
			endRequest();
		}
	}

	@Override
	public synchronized void cancelRequest() {
		if (state != State.RUNNING) {
			return;
		}
		state = State.CANCELING;
		/*
		 * Killing the process is the only documented way to stop a running turn in headless mode
		 * (the SDKs use an undocumented "control_request" interrupt instead). The session is not lost:
		 * Claude Code stores it on disk and the next request resumes it with "--resume <session id>".
		 */
		terminateCurrentProcess(false);
	}

	@Nullable
	@Override
	public synchronized Object getResumeState() {
		return sessionExists ? new ClaudeCodeResumeState(sessionId) : null;
	}

	@Override
	public synchronized void close() {
		if (state == State.CLOSED) {
			return;
		}
		state = State.CLOSED;
		// graceful: give Claude Code the chance to store the session for resuming it later
		terminateCurrentProcess(true);
	}

	/*
	 * Request Handling
	 */
	private synchronized ClaudeCodeProcess beginRequest() throws IOException {
		if (state == State.CLOSED) {
			throw new IOException("The agent has been closed");
		}
		if (state != State.IDLE) {
			throw new IllegalStateException("Another request is running");
		}
		if (currentProcess == null || !currentProcess.isAlive()) {
			// a process that has terminated while no request was running has released its resources itself
			currentProcess = startProcess();
		}
		state = State.RUNNING;
		return currentProcess;
	}

	private synchronized void endRequest() {
		if (state != State.CLOSED) {
			state = State.IDLE;
		}
	}

	/**
	 * @throws InterruptedIOException if the request has been canceled or the agent has been closed
	 */
	private synchronized void checkNotAborted() throws InterruptedIOException {
		switch (state) {
			case CANCELING:
				throw new InterruptedIOException("The request has been canceled");
			case CLOSED:
				throw new InterruptedIOException("The agent has been closed");
			default:
				break;
		}
	}

	private synchronized void markSessionExists() {
		sessionExists = true;
	}

	private synchronized void onProcessTerminated(ClaudeCodeProcess process) {
		if (currentProcess == process) {
			// terminated unexpectedly (otherwise, it would already have been removed); it has released its resources itself
			currentProcess = null;
		}
		if (!sessionExists) {
			/*
			 * Claude Code may or may not have stored the session already. Neither "--session-id" (session
			 * may already exist) nor "--resume" (session may not exist) would work reliably with the
			 * current ID. Since there is no answer yet, we can safely start a new session instead.
			 */
			sessionId = UUID.randomUUID().toString();
		}
	}

	private void writeUserMessage(ClaudeCodeProcess process, String message) throws IOException {
		JsonObject messageObject = new JsonObject();
		messageObject.addProperty("role", "user");
		messageObject.addProperty("content", message);
		JsonObject event = new JsonObject();
		event.addProperty("type", "user");
		event.add("message", messageObject);
		try {
			process.writeLine(GSON.toJson(event));
		} catch (IOException e) {
			checkNotAborted();
			throw new IOException("Cannot send the request to Claude Code: " + e.getMessage() + process.getErrorDetails(), e);
		}
	}

	private String awaitResult(ClaudeCodeProcess process, AiAgentListener listener) throws IOException {
		/*
		 * Texts of the current request that have not been reported as progress yet. They are reported
		 * when a tool use follows. Otherwise, they are part of the final answer (result event) and
		 * must not be reported to avoid duplicates.
		 */
		List<String> pendingTexts = new ArrayList<>();
		while (true) {
			String line;
			try {
				line = process.pollLine(POLL_INTERVAL_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InterruptedIOException("Interrupted while waiting for Claude Code");
			} catch (EOFException e) {
				onProcessTerminated(process);
				checkNotAborted();
				throw new IOException("Claude Code terminated unexpectedly" + process.getErrorDetails());
			}
			if (line == null) {
				checkNotAborted();
				continue;
			}
			String result;
			try {
				result = handleEvent(line, listener, pendingTexts);
			} catch (JsonParseException e) {
				throw new IOException("Unexpected message from Claude Code: " + e.getMessage(), e);
			}
			if (result != null) {
				return result;
			}
		}
	}

	/**
	 * Lines that are not JSON objects and events of other types are ignored.
	 *
	 * @return the final answer if the event is the result event, otherwise {@code null}
	 * @throws JsonParseException if a member that is evaluated (the type of any event or the content of an
	 *                            assistant or result event) has an unexpected type
	 * @throws IOException if the event is a result event that reports an error
	 */
	@Nullable
	private String handleEvent(String line, AiAgentListener listener, List<String> pendingTexts) throws IOException {
		JsonObject event;
		try {
			JsonElement element = JsonParser.parseString(line);
			if (!element.isJsonObject()) {
				return null;
			}
			event = element.getAsJsonObject();
		} catch (JsonParseException e) {
			// not JSON: ignore
			return null;
		}
		String type = getString(event, "type");
		if ("assistant".equals(type)) {
			markSessionExists();
			handleAssistantMessage(event, listener, pendingTexts);
		} else if ("result".equals(type)) {
			markSessionExists();
			return handleResultMessage(event, pendingTexts);
		}
		return null;
	}

	/**
	 * @return the final answer
	 * @throws IOException if Claude Code reports an error
	 */
	private static String handleResultMessage(JsonObject event, List<String> pendingTexts) throws IOException {
		// pending texts are part of the final answer => discard them instead of reporting them
		pendingTexts.clear();
		String result = getString(event, "result");
		boolean isError = getBoolean(event, "is_error", false);
		String subtype = getString(event, "subtype");
		if (isError || !"success".equals(subtype)) {
			throw new IOException("Claude Code reported an error"
				+ (subtype != null ? " (" + subtype + ")" : "")
				+ (result != null ? ": " + result : ""));
		}
		return result != null ? result : "";
	}

	private void handleAssistantMessage(JsonObject event, AiAgentListener listener, List<String> pendingTexts) {
		JsonObject message = getObject(event, "message");
		for (JsonObject block : getObjects(message, "content")) {
			String blockType = getString(block, "type");
			if ("text".equals(blockType)) {
				String text = getString(block, "text");
				if (text != null && !text.trim().isEmpty()) {
					pendingTexts.add(text);
				}
			} else if ("tool_use".equals(blockType)) {
				// the pending texts have been intermediate texts, not the final answer
				for (String text : pendingTexts) {
					listener.onProgress(text);
				}
				pendingTexts.clear();
				String toolName = getString(block, "name");
				JsonObject input = getObject(block, "input");
				String displayName = getDisplayToolName(toolName);
				listener.onToolUse(displayName, summarizeToolInput(input), input);
			}
		}
	}

	private String getDisplayToolName(@Nullable String toolName) {
		if (toolName == null) {
			return "<unknown tool>";
		}
		String mcpPrefix = "mcp__" + environment.getMcpServerName() + "__";
		return toolName.startsWith(mcpPrefix) ? toolName.substring(mcpPrefix.length()) : toolName;
	}

	private String summarizeToolInput(JsonObject input) {
		// best effort: the summary is only displayed, so unexpected types must not let the request fail
		for (String key : Arrays.asList("expression", "file_path", "pattern", "name", "path")) {
			JsonElement value = input.get(key);
			if (value != null && value.isJsonPrimitive()) {
				return truncate(value.getAsString());
			}
		}
		return input.isEmpty() ? "" : truncate(GSON.toJson(input));
	}

	private static String truncate(String s) {
		return s.length() <= MAX_SUMMARY_LENGTH ? s : s.substring(0, MAX_SUMMARY_LENGTH) + "...";
	}

	/*
	 * Process Handling
	 */
	private ClaudeCodeProcess startProcess() throws IOException {
		List<Path> tempFiles = new ArrayList<>();
		try {
			McpEndpoint endpoint = environment.getMcpEndpoint();
			Path mcpConfigFile = ClaudeCodeProcess.writeTempFile("marple-mcp-", ".json", createMcpConfig(endpoint), tempFiles);
			Path systemPromptFile = ClaudeCodeProcess.writeTempFile("marple-prompt-", ".txt", environment.getSystemPrompt(), tempFiles);

			List<String> command = new ArrayList<>();
			command.add(executable);
			command.add("-p");
			command.addAll(Arrays.asList("--input-format", "stream-json"));
			command.addAll(Arrays.asList("--output-format", "stream-json"));
			command.add("--verbose");
			command.addAll(Arrays.asList("--mcp-config", mcpConfigFile.toString()));
			command.add("--strict-mcp-config");
			// tools that require approval are not allowed explicitly => Claude Code asks the permission prompt tool
			List<String> allowedTools = new ArrayList<>();
			allowedTools.add("mcp__" + environment.getMcpServerName());
			tools.forEach((tool, requiresApproval) -> {
				if (!requiresApproval) {
					allowedTools.add(tool);
				}
			});
			/*
			 * No built-in tools requires an empty value. "--tools=" avoids passing an empty argument, which
			 * is not handled consistently by command line parsers on Windows.
			 */
			command.add("--tools=" + String.join(",", tools.keySet()));
			command.addAll(Arrays.asList("--allowedTools", String.join(",", allowedTools)));
			command.addAll(Arrays.asList("--permission-prompt-tool", "mcp__" + environment.getMcpServerName() + "__" + PERMISSION_TOOL_NAME));
			command.addAll(Arrays.asList("--append-system-prompt-file", systemPromptFile.toString()));
			command.addAll(Arrays.asList(sessionExists ? "--resume" : "--session-id", sessionId));
			command.addAll(additionalArguments);

			return ClaudeCodeProcess.start(command, workingDirectory, tempFiles);
		} catch (IOException | RuntimeException e) {
			ClaudeCodeProcess.deleteTempFiles(tempFiles);
			throw e;
		}
	}

	/**
	 * Removes the current process (see {@link #currentProcess}) and terminates it.
	 */
	private synchronized void terminateCurrentProcess(boolean graceful) {
		ClaudeCodeProcess process = currentProcess;
		if (process != null) {
			currentProcess = null;
			process.terminate(graceful);
		}
	}

	private String createMcpConfig(McpEndpoint endpoint) {
		JsonObject headers = new JsonObject();
		headers.addProperty("Authorization", "Bearer " + endpoint.getBearerToken());
		JsonObject server = new JsonObject();
		server.addProperty("type", "http");
		server.addProperty("url", endpoint.getUrl());
		server.add("headers", headers);
		JsonObject servers = new JsonObject();
		servers.add(environment.getMcpServerName(), server);
		JsonObject config = new JsonObject();
		config.add("mcpServers", servers);
		return GSON.toJson(config);
	}

	/*
	 * Permission Prompts
	 */
	private AiTool createPermissionTool() {
		JsonObject inputSchema = new JsonObject();
		inputSchema.addProperty("type", "object");
		return AiTool.builder(PERMISSION_TOOL_NAME,
				"Internal tool used by Claude Code to ask the user for permission. Do not call this tool yourself.",
				this::handlePermissionRequest)
			.parameter("tool_name", "string", "Name of the tool that requests permission", true)
			.parameter("input", inputSchema, true)
			.parameter("tool_use_id", "string", "ID of the tool use", false)
			.build();
	}

	private AiToolResult handlePermissionRequest(JsonObject arguments) {
		String toolName = getString(arguments, "tool_name");
		JsonObject input = getObject(arguments, "input");
		if (toolName == null) {
			toolName = "<unknown>";
		}
		PermissionDecision decision;
		if (Boolean.TRUE.equals(tools.get(toolName))) {
			String filePath = null;
			if (FILE_EDIT_TOOLS.contains(toolName)) {
				filePath = getString(input, "file_path");
				if (filePath == null) {
					filePath = getString(input, "notebook_path");
				}
			}
			decision = environment.getPermissionHandler().requestPermission(new PermissionRequest(toolName, filePath, createPreview(toolName, input)));
		} else {
			/*
			 * Safety net: Only tools that have been registered with approval should reach this tool. Others
			 * are either not available or allowed without asking.
			 */
			decision = PermissionDecision.deny("The tool '" + toolName + "' is not available in Marple.");
		}

		JsonObject response = new JsonObject();
		if (decision.isAllowed()) {
			response.addProperty("behavior", "allow");
			response.add("updatedInput", input);
		} else {
			response.addProperty("behavior", "deny");
			response.addProperty("message", decision.getDenyMessage());
		}
		return AiToolResult.success(GSON.toJson(response));
	}

	private String createPreview(String toolName, JsonObject input) {
		switch (toolName) {
			case "Edit": {
				StringBuilder preview = new StringBuilder();
				appendDiff(preview, getString(input, "old_string"), getString(input, "new_string"));
				if (getBoolean(input, "replace_all", false)) {
					preview.append("\n(replace all occurrences)");
				}
				return preview.toString();
			}
			case "MultiEdit": {
				StringBuilder preview = new StringBuilder();
				int index = 1;
				for (JsonObject edit : getObjects(input, "edits")) {
					preview.append("=== Edit ").append(index++).append(" ===\n");
					appendDiff(preview, getString(edit, "old_string"), getString(edit, "new_string"));
					preview.append("\n");
				}
				return preview.toString();
			}
			case "Write": {
				String content = getString(input, "content");
				return content != null ? content : "";
			}
			default:
				return PRETTY_GSON.toJson(input);
		}
	}

	private static void appendDiff(StringBuilder preview, @Nullable String oldString, @Nullable String newString) {
		if (oldString != null) {
			for (String line : oldString.split("\n", -1)) {
				preview.append("- ").append(line).append("\n");
			}
		}
		if (newString != null) {
			for (String line : newString.split("\n", -1)) {
				preview.append("+ ").append(line).append("\n");
			}
		}
	}
}
