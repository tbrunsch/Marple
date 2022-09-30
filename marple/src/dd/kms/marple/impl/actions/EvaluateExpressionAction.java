package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;

public class EvaluateExpressionAction implements InspectionAction
{
	private final ExpressionEvaluator	expressionEvaluator;
	private final String				expression;
	private final Object				thisValue;
	private final int					caretPosition;

	public EvaluateExpressionAction(ExpressionEvaluator expressionEvaluator, String expression, Object thisValue, int caretPosition) {
		this.expressionEvaluator = expressionEvaluator;
		this.expression = expression;
		this.thisValue = thisValue;
		this.caretPosition = caretPosition;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return expression == null ? "Evaluate..." : "Evaluate '" + expression + "'";
	}

	@Override
	public String getDescription() {
		return "Opens the expression evaluation dialog. The expression 'this' refers to object containing this object as field.";
	}

	@Override
	public boolean isEnabled() {
		return expressionEvaluator != null;
	}

	@Override
	public void perform() {
		expressionEvaluator.evaluate(expression, thisValue, caretPosition);
	}
}
