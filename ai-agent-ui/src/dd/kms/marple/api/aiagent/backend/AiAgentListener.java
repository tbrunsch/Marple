package dd.kms.marple.api.aiagent.backend;

/**
 * Receives progress information while the agent processes a request. Methods may be called
 * from any thread.
 */
public interface AiAgentListener
{
	/**
	 * Called for texts the agent produces while processing a request, e.g., explanations between
	 * tool uses. The final answer, which is returned by {@link AiAgent#sendRequest(String, AiAgentListener)},
	 * must not be reported via this method.
	 *
	 * @param text the text (Markdown)
	 */
	void onProgress(String text);

	/**
	 * Called when the agent uses a tool.
	 *
	 * @param toolName	the name of the tool as displayed to the user
	 * @param summary	a short summary of the tool input (e.g., an expression or a file path); may be empty
	 */
	void onToolUse(String toolName, String summary);
}
