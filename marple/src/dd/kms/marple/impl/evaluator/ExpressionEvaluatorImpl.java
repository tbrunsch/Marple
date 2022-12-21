package dd.kms.marple.impl.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.marple.impl.gui.evaluator.EvaluationFrame;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExpressionEvaluatorImpl implements ExpressionEvaluator
{
	private ParserSettings					parserSettings	= ParserSettingsBuilder.create().build();
	private final List<DisposableVariable>	variables		= new ArrayList<>();
	private InspectionContext				context;

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
	public List<Variable> getVariables() {
		List<Variable> variables = new ArrayList<>(this.variables.size());
		Iterator<DisposableVariable> iter = this.variables.iterator();
		while (iter.hasNext()) {
			DisposableVariable variable = iter.next();
			DefaultVariable variableSnapshot = new DefaultVariable(variable.getName(), variable.getType(), variable.getValue(), variable.isFinal(), variable.isUseHardReference());
			if (variable.isGarbageCollected()) {
				iter.remove();
			} else {
				variables.add(variableSnapshot);
			}
		}
		return variables;
	}

	@Override
	public void setVariables(List<Variable> variables) {
		this.variables.clear();
		for (Variable variable : variables) {
			DisposableVariable disposableVariable = new DisposableVariable(variable.getName(), variable.getType(), variable.getValue(), variable.isFinal(), variable.isUseHardReference());
			this.variables.add(disposableVariable);
		}
	}

	public Variables getVariableCollection() {
		Variables variables = Variables.create();
		for (Variable variable : getVariables()) {
			variables.createVariable(variable.getName(), variable.getClass(), variable.getValue(), variable.isFinal());
		}
		return variables;
	}

	@Override
	public void evaluate(String expression, int caretPosition, Object thisValue) {
		showEvaluationFrame(expression, caretPosition, thisValue);
	}

	/*
	 * Evaluation Frame Handling
	 */
	private void showEvaluationFrame(String expression, int caretPosition, Object thisValue) {
		EvaluationFrame evaluationFrame = WindowManager.getWindow(ExpressionEvaluator.class, this::createEvaluationFrame);
		evaluationFrame.evaluate(expression, caretPosition, thisValue);
	}

	private EvaluationFrame createEvaluationFrame() {
		EvaluationFrame evaluationFrame = new EvaluationFrame(context);
		evaluationFrame.setPreferredSize(EvaluationFrame.INITIAL_PREFERRED_SIZE);
		return evaluationFrame;
	}
}
