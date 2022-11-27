package dd.kms.marple.impl.evaluator;

import com.google.common.util.concurrent.Runnables;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.marple.impl.gui.evaluator.EvaluationFrame;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

public class ExpressionEvaluatorImpl implements ExpressionEvaluator
{
	private ParserSettings		parserSettings	= ParserSettingsBuilder.create().build();
	private InspectionContext	context;

	@Override
	public void setInspectionContext(InspectionContext context) {
		this.context = context;
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
	public void evaluate(String expression, int caretPosition, Object thisValue) {
		showEvaluationFrame(expression, caretPosition, thisValue);
	}

	/*
	 * Evaluation Frame Handling
	 */
	private void showEvaluationFrame(String expression, int caretPosition, Object thisValue) {
		EvaluationFrame evaluationFrame = WindowManager.getWindow(ExpressionEvaluator.class, this::createEvaluationFrame, Runnables.doNothing());
		evaluationFrame.evaluate(expression, caretPosition, thisValue);
	}

	private EvaluationFrame createEvaluationFrame() {
		EvaluationFrame evaluationFrame = new EvaluationFrame(context);
		evaluationFrame.setPreferredSize(EvaluationFrame.INITIAL_PREFERRED_SIZE);
		return evaluationFrame;
	}
}
