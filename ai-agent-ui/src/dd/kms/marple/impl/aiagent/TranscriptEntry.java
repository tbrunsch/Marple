package dd.kms.marple.impl.aiagent;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

/**
 * An immutable entry of the transcript. Entries with role {@link Role#TOOL} additionally describe the
 * tool call (name and complete input).
 */
class TranscriptEntry
{
	enum Role
	{
		USER,
		AGENT,
		PROGRESS,
		TOOL,
		INFO,
		ERROR
	}

	private final Role			role;
	private final String		text;
	@Nullable
	private final String		toolName;
	@Nullable
	private final JsonObject	toolInput;

	TranscriptEntry(Role role, String text) {
		this(role, text, null, null);
	}

	private TranscriptEntry(Role role, String text, @Nullable String toolName, @Nullable JsonObject toolInput) {
		this.role = role;
		this.text = text;
		this.toolName = toolName;
		this.toolInput = toolInput;
	}

	/**
	 * @param text	the text displayed in the transcript
	 * @param input	the complete tool input; it is copied
	 */
	static TranscriptEntry createToolEntry(String text, String toolName, JsonObject input) {
		return new TranscriptEntry(Role.TOOL, text, toolName, input.deepCopy());
	}

	Role getRole() {
		return role;
	}

	String getText() {
		return text;
	}

	/**
	 * Returns the name of the tool for entries created via {@link #createToolEntry(String, String, JsonObject)}.
	 */
	@Nullable
	String getToolName() {
		return toolName;
	}

	/**
	 * Returns a copy of the tool input for entries created via {@link #createToolEntry(String, String, JsonObject)}.
	 */
	@Nullable
	JsonObject getToolInput() {
		return toolInput != null ? toolInput.deepCopy() : null;
	}
}
