package dd.kms.marple.api.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.settings.ParserSettings;

public interface ExpressionEvaluator
{
	static ExpressionEvaluator create() {
		return new dd.kms.marple.impl.evaluator.ExpressionEvaluatorImpl();
	}

	void setInspectionContext(InspectionContext context);
	ParserSettings getParserSettings();
	void setParserSettings(ParserSettings parserSettings);
	void evaluate(String expression, Object thisValue, int caretPosition);
}
