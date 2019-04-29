package dd.kms.marple.swing.evaluator;

import dd.kms.marple.ExpressionEvaluator;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.swing.gui.GuiCommons;
import dd.kms.marple.swing.gui.evaluation.EvaluationPanel;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsBuilder;

import java.awt.*;
import java.util.Optional;

public class SwingExpressionEvaluator implements ExpressionEvaluator<Component>
{
	private ParserSettings					parserSettings		= new ParserSettingsBuilder().build();
	private InspectionContext<Component>	inspectionContext;

	@Override
	public void setInspectionContext(InspectionContext<Component> inspectionContext) {
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
