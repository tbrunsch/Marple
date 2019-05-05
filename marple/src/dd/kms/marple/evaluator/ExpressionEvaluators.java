package dd.kms.marple.evaluator;

public class ExpressionEvaluators
{
	public static ExpressionEvaluator create() {
		return new ExpressionEvaluatorImpl();
	}
}
