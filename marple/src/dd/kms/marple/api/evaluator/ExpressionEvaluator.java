package dd.kms.marple.api.evaluator;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.settings.ParserSettings;

import javax.annotation.Nullable;
import java.util.List;

public interface ExpressionEvaluator
{
	static ExpressionEvaluator create() {
		return new dd.kms.marple.impl.evaluator.ExpressionEvaluatorImpl();
	}

	ParserSettings getParserSettings();
	void setParserSettings(ParserSettings parserSettings);

	List<Variable> getVariables();
	void setVariables(List<Variable> variables);

	/**
	 * Adds the specified variable. If a variable with the same name already exists,
	 * then this variable will be replaced.
	 */
	void addVariable(Variable variable);

	/**
	 * Removes the variable with the specified name. Returns {@code true} if such
	 * a variable existed and has been removed and {@code false} otherwise.
	 */
	boolean removeVariable(String name);

	/**
	 * Evaluates the specified expression in the context of {@code thisValue} with the
	 * evaluator's current parser settings and variables and returns the result. In contrast to
	 * {@link #showEvaluationDialog(String, int, Object)}, no UI is involved and the expression
	 * is not added to the expression history.
	 */
	@Nullable
	Object evaluate(String expression, @Nullable Object thisValue) throws ParseException;

	/**
	 * Opens the evaluation dialog with the specified expression and caret position. The
	 * expression will be evaluated in the context of {@code thisValue}.
	 */
	void showEvaluationDialog(String expression, int caretPosition, Object thisValue);

	StringHistory getExpressionHistory();
}
