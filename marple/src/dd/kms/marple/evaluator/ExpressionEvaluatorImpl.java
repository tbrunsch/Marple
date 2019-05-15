package dd.kms.marple.evaluator;

import com.google.common.util.concurrent.Runnables;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.common.WindowManager;
import dd.kms.marple.gui.evaluator.EvaluationFrame;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsUtils;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class ExpressionEvaluatorImpl implements ExpressionEvaluator
{
	private ParserSettings		parserSettings		= ParserSettingsUtils.createBuilder().build();
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
		showEvaluationFrame(expression, thisValue);
	}

	/*
	 * Evaluation Frame Handling
	 */
	private void showEvaluationFrame(String expression, Object thisValue) {
		EvaluationFrame evaluationFrame = WindowManager.getWindow(ExpressionEvaluator.class, this::createEvaluationFrame, Runnables.doNothing());
		evaluationFrame.setThisValue(thisValue);
		evaluationFrame.setExpression(expression);
	}

	private EvaluationFrame createEvaluationFrame() {
		EvaluationFrame evaluationFrame = new EvaluationFrame(inspectionContext);
		evaluationFrame.setPreferredSize(new Dimension(600, 400));
		return evaluationFrame;
	}
}
