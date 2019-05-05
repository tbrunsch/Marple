package dd.kms.marple.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.common.GuiCommons;
import dd.kms.marple.gui.evaluator.EvaluationPanel;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsBuilder;

class ExpressionEvaluatorImpl implements ExpressionEvaluator
{
	private ParserSettings		parserSettings		= new ParserSettingsBuilder().build();
	private InspectionContext	inspectionContext;

	@Override
	public void setInspectionContext(InspectionContext inspectionContext) {
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
		EvaluationPanel evaluationPanel = new EvaluationPanel(thisValue, inspectionContext);
		evaluationPanel.setExpression(expression);

		GuiCommons.showPanel("Evaluate", evaluationPanel);
	}
}
