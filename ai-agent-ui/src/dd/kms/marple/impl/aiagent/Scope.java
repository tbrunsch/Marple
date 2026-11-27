package dd.kms.marple.impl.aiagent;

/**
 * What the user wants the agent to work on. The scope is selected per message: It is sent to the agent with
 * the message and decides whether file changes are possible.
 */
enum Scope
{
	/**
	 * Analyze or change the running application. File changes are rejected without asking.
	 */
	RUNNING_APPLICATION("Running application", "running application",
		"Analyze or change the running application. The agent does not change files."),

	/**
	 * The user wants the result in files. The agent may still use the running application, e.g., for trying out
	 * changes before editing the files. File changes require approval.
	 */
	FILES("Files", "files",
		"The result should be in files (source code, resources, configuration). The agent may still use the running application, e.g., to try out changes.");

	private final String	displayText;
	private final String	agentText;
	private final String	description;

	Scope(String displayText, String agentText, String description) {
		this.displayText = displayText;
		this.agentText = agentText;
		this.description = description;
	}

	String getDescription() {
		return description;
	}

	/**
	 * The line that tells the agent the scope of a message. The system prompt explains the scopes.
	 */
	String getMessageLine() {
		return "[scope: " + agentText + "]";
	}

	@Override
	public String toString() {
		return displayText;
	}
}
