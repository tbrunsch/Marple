package dd.kms.marple.api.settings.keys;

import java.awt.event.KeyEvent;

public enum KeyFunction
{
	/**
	 * Open the inspection dialog for the GUI element under the mouse cursor.
	 */
	INSPECTION(new KeyRepresentation(KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, KeyEvent.VK_I)),

	/**
	 * Open the expression evaluation dialog for the GUI element under the mouse cursor.
	 */
	EVALUATION(new KeyRepresentation(KeyEvent.ALT_MASK, KeyEvent.VK_F8)),

	/**
	 * Open the find instances dialog with the GUI element under the mouse cursor as root of the search.
	 */
	FIND_INSTANCES(new KeyRepresentation(KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, KeyEvent.VK_F)),

	/**
	 * Open the debug support dialog.
	 */
	DEBUG_SUPPORT(new KeyRepresentation(KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, KeyEvent.VK_D)),

	/**
	 * Open the dialog where you can configure custom actions.
	 */
	CUSTOM_ACTIONS(new KeyRepresentation(KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, KeyEvent.VK_A)),

	/**
	 * When evaluation expressions: Show a list of suggested code completions.
	 */
	CODE_COMPLETION(new KeyRepresentation(KeyEvent.CTRL_MASK, KeyEvent.VK_SPACE)),

	/**
	 * When inside a method argument list during expression evaluation: Show a tool tip with the method arguments.
	 */
	SHOW_METHOD_ARGUMENTS(new KeyRepresentation(KeyEvent.CTRL_MASK, KeyEvent.VK_P)),

	/**
	 * Open the quick help dialog.
	 */
	QUICK_HELP(new KeyRepresentation(KeyEvent.CTRL_MASK, KeyEvent.VK_F1));

	private final KeyRepresentation	defaultKey;

	KeyFunction(KeyRepresentation defaultKey) {
		this.defaultKey = defaultKey;
	}

	public KeyRepresentation getDefaultKey() {
		return defaultKey;
	}
}
