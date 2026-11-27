package dd.kms.marple.api.aiagent.backend;

import javax.annotation.Nullable;

/**
 * Request of the agent to perform an action that requires approval, e.g., creating or modifying a file
 * (see {@link AiAgentEnvironment#getPermissionHandler()})
 */
public final class PermissionRequest
{
	private final String	toolName;
	@Nullable
	private final String	filePath;
	private final String	preview;

	/**
	 * @param filePath	the file the tool creates or modifies or {@code null} if the tool does not change a file
	 *                  (see {@link #isFileChange()})
	 */
	public PermissionRequest(String toolName, @Nullable String filePath, String preview) {
		this.toolName = toolName;
		this.filePath = filePath;
		this.preview = preview;
	}

	public String getToolName() {
		return toolName;
	}

	@Nullable
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Returns {@code true} if the tool creates or modifies a file. File changes are rejected without asking
	 * if the user has restricted the agent to the running application.
	 */
	public boolean isFileChange() {
		return filePath != null;
	}

	/**
	 * Human-readable description of the change (e.g., a diff) or, for actions that do not change files,
	 * of the tool's arguments
	 */
	public String getPreview() {
		return preview;
	}
}
