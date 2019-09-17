package dd.kms.marple.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

public interface ExpressionEvaluator
{
	void setInspectionContext(InspectionContext inspectionContext);
	ParserSettings getParserSettings();
	void setParserSettings(ParserSettings parserSettings);
	void evaluate(String expression, ObjectInfo thisValue);
	void evaluate(String expression, ObjectInfo thisValue, int caretPosition);
}
