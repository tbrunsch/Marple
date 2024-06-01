package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;

public class EvaluateExpressionAction implements InspectionAction
{
	private final ExpressionEvaluator	expressionEvaluator;
	private final String				expression;
	private final int					caretPosition;
	private final Object				thisValue;

	public EvaluateExpressionAction(ExpressionEvaluator expressionEvaluator, String expression, int caretPosition, Object thisValue) {
		this.expressionEvaluator = expressionEvaluator;
		this.expression = expression;
		this.caretPosition = caretPosition;
		this.thisValue = thisValue;
	}

	@Override
	public String getName() {
		return "this".equals(expression) ? "Evaluate" : "Evaluate as '" + expression + "'";
	}

	@Override
	public String getDescription() {
		return "Opens the expression evaluation dialog. The expression '" + expression + "' refers to this object.";
	}

	@Override
	public boolean isEnabled() {
		return expressionEvaluator != null;
	}

	@Override
	public void perform() {
		expressionEvaluator.evaluate(expression, caretPosition, thisValue);
	}
}
