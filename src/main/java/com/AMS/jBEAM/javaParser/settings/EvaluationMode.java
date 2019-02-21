package com.AMS.jBEAM.javaParser.settings;

public enum EvaluationMode
{
	/**
	 * do not evaluate expressions at all
	 *
	 * Useful for code completion
	 */
	NONE,

	/**
	 * use declared types
	 *
	 * Parser errors are possible although expression could be parsed when assuming actual type.
	 * Casts are required to prevent such parser errors.
	 *
	 * Example: Object o = "12345";
	 *
	 * The expression "o.length()" will result in a parser error because the method Object.length()
	 * does not exist. If o was declared as String, then there would be no error and the result
	 * was 5. Otherwise, one has to fix the expression to "((String) o).length()", leading to the
	 * desired result.
	 */
	STATICALLY_TYPED,

	/**
	 * use runtime types
	 *
	 * Safes unnecessary casts, but can be dangerous with overloaded methods.
	 *
	 * Example: Object o = "12345",
	 * 			method String getType(Object obj) { return "object"; }, and
	 *          method String getType(String s) { return "string"; }
	 *
	 * 			With dynamic typing, the expression "getType(o)" will be evaluated to "string",
	 * 			with static typing it will be evaluated to "object".
	 */
	DYNAMICALLY_TYPED
}
