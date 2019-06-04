package dd.kms.marple.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.settings.ParserSettings;

public interface ExpressionEvaluator
{
	void setInspectionContext(InspectionContext inspectionContext);
	ParserSettings getParserSettings();
	void setParserSettings(ParserSettings parserSettings);
	void evaluate(String expression, Object thisValue);
	void evaluate(String expression, Object thisValue, int caretPosition);
}
