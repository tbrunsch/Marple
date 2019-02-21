package com.AMS.jBEAM.javaParser.settings;

public enum ParseMode
{
	/**
	 * Used for code completion. Only evaluates expressions if dynamic typing is activated.
	 */
	CODE_COMPLETION,

	/**
	 * Used for regular expression evaluation. Evaluation is either based on declared types (static typing)
	 * or on runtime types (dynamic typing).
	 */
	EVALUATION,

	/**
	 * Internal parse mode that suppresses evaluation to avoid side effects.
	 */
	WITHOUT_EVALUATION
}
