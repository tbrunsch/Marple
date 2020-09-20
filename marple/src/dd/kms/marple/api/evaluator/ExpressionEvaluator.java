package dd.kms.marple.api.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public interface ExpressionEvaluator
{
	static ExpressionEvaluator create() {
		return new dd.kms.marple.impl.evaluator.ExpressionEvaluatorImpl();
	}

	void setInspectionContext(InspectionContext context);
	ParserSettings getParserSettings();
	void setParserSettings(ParserSettings parserSettings);
	void evaluate(String expression, ObjectInfo thisValue);
	void evaluate(String expression, ObjectInfo thisValue, int caretPosition);
}
