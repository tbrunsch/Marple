package dd.kms.marple.swing.evaluator;

import dd.kms.marple.ExpressionEvaluator;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsBuilder;

public class SwingExpressionEvaluator implements ExpressionEvaluator
{
	private ParserSettings			parserSettings		= new ParserSettingsBuilder().build();
	private InspectionContext<?>	inspectionContext;

	@Override
	public void setInspectionContext(InspectionContext<?> inspectionContext) {
		this.inspectionContext = inspectionContext;
	}

	@Override
	public ParserSettings getParserSettings() {
		return parserSettings;
	}

	@Override
	public void setParserSettings(ParserSettings parserSettings) {
		this.parserSettings = parserSettings;
	}

	@Override
	public void evaluate(String expression, Object thisValue) {
		// TODO
	}
}
