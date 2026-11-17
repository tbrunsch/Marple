package dd.kms.marple.impl.aiagent;

/**
 * The system prompt for the agent. Its text is stored in the resource file {@code SystemPrompt.md}.
 * A missing resource is not ignored because the agent must not run without its instructions.
 */
final class SystemPrompt
{
	static final String	TEXT	= Resources.loadText("SystemPrompt.md");

	private SystemPrompt() {}
}
