package dd.kms.marple;

public interface ExpressionEvaluator
{
	void setInspectionContext(InspectionContext<?, ?> inspectionContext);
	void evaluate(String expression, Object thisValue);
}
