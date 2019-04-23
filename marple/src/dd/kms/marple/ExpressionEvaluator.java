package dd.kms.marple;

import dd.kms.zenodot.settings.ParserSettings;

public interface ExpressionEvaluator
{
	void setInspectionContext(InspectionContext<?> inspectionContext);
	ParserSettings getParserSettings();
	void setParserSettings(ParserSettings parserSettings);
	void evaluate(String expression, Object thisValue);
}
