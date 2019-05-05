package dd.kms.marple.actions;

import dd.kms.marple.evaluator.ExpressionEvaluator;

public class EvaluateAsThisAction implements InspectionAction
{
	private final ExpressionEvaluator	expressionEvaluator;
	private final Object				thisValue;

	public EvaluateAsThisAction(ExpressionEvaluator expressionEvaluator, Object thisValue) {
		this.expressionEvaluator = expressionEvaluator;
		this.thisValue = thisValue;
	}

	@Override
	public String getName() {
		return "Evaluate";
	}

	@Override
	public String getDescription() {
		return "Opens the expression evaluation dialog. The expression 'this' refers to this object.";
	}

	@Override
	public boolean isEnabled() {
		return expressionEvaluator != null;
	}

	@Override
	public void perform() {
		expressionEvaluator.evaluate("this", thisValue);
	}
}
