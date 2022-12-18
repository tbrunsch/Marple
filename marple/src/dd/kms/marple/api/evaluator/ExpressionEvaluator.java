package dd.kms.marple.api.evaluator;

import dd.kms.zenodot.api.settings.ParserSettings;

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

	void evaluate(String expression, int caretPosition, Object thisValue);
}
