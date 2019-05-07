package dd.kms.marple.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.evaluator.EvaluationFrame;
import dd.kms.marple.gui.evaluator.EvaluationPanel;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsUtils;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class ExpressionEvaluatorImpl implements ExpressionEvaluator
{
	private ParserSettings		parserSettings		= ParserSettingsUtils.createBuilder().build();
	private InspectionContext	inspectionContext;
	private EvaluationFrame		evaluationFrame;

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
		boolean virginEvaluationFrame = evaluationFrame == null;
		if (virginEvaluationFrame) {
			evaluationFrame = createEvaluationFrame();
			evaluationFrame.setPreferredSize(new Dimension(600, 400));
		}
		evaluationFrame.setThisValue(thisValue);
		evaluationFrame.setExpression(expression);
		if (virginEvaluationFrame) {
			evaluationFrame.pack();
			evaluationFrame.setVisible(true);
		}
	}

	private EvaluationFrame createEvaluationFrame() {
		EvaluationFrame evaluationFrame = new EvaluationFrame(inspectionContext);
		evaluationFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				disposeEvaluationFrame();
			}
		});
		return evaluationFrame;
	}

	private void disposeEvaluationFrame() {
		evaluationFrame = null;
	}
}
