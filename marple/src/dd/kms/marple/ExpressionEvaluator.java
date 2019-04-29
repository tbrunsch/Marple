package dd.kms.marple;

import dd.kms.zenodot.settings.ParserSettings;

/**
 *
 * @param <C>	GUI component class
 */
public interface ExpressionEvaluator<C>
{
	void setInspectionContext(InspectionContext<C> inspectionContext);
	ParserSettings getParserSettings();
	void setParserSettings(ParserSettings parserSettings);
	void evaluate(String expression, Object thisValue);
}
