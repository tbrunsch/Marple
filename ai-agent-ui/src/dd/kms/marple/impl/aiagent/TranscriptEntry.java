package dd.kms.marple.impl.aiagent;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

/**
 * An immutable entry of the transcript. Entries with role {@link Role#USER} additionally contain the scope
 * of the message, entries with role {@link Role#TOOL} describe the tool call (name and complete input).
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
	@Nullable
	private final Scope			scope;

	/**
	 * Creates an entry that is neither a user message nor a tool call (see {@link #createUserEntry(String, Scope)}
	 * and {@link #createToolEntry(String, String, JsonObject)}).
	 */
	TranscriptEntry(Role role, String text) {
		this(checkGeneralRole(role), text, null, null, null);
	}

	private TranscriptEntry(Role role, String text, @Nullable String toolName, @Nullable JsonObject toolInput, @Nullable Scope scope) {
		this.role = role;
		this.text = text;
		this.toolName = toolName;
		this.toolInput = toolInput;
		this.scope = scope;
	}

	private static Role checkGeneralRole(Role role) {
		if (role == Role.USER) {
			throw new IllegalArgumentException("User entries must be created via createUserEntry()");
		}
		return role;
	}

	static TranscriptEntry createUserEntry(String message, Scope scope) {
		return new TranscriptEntry(Role.USER, message, null, null, scope);
	}

	/**
	 * @param text	the text displayed in the transcript
	 * @param input	the complete tool input; it is copied
	 */
	static TranscriptEntry createToolEntry(String text, String toolName, JsonObject input) {
		return new TranscriptEntry(Role.TOOL, text, toolName, input.deepCopy(), null);
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
	 * The result is {@code null} if and only if {@link #getToolName()} returns {@code null}.
	 */
	@Nullable
	JsonObject getToolInput() {
		return toolInput != null ? toolInput.deepCopy() : null;
	}

	/**
	 * Returns the scope of the message for entries created via {@link #createUserEntry(String, Scope)}.
	 * The result is non-null if and only if the role is {@link Role#USER}.
	 */
	@Nullable
	Scope getScope() {
		return scope;
	}
}
