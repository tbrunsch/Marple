package dd.kms.marple.swing.evaluator;

import dd.kms.marple.ExpressionEvaluator;
import dd.kms.marple.InspectionContext;

public class SwingExpressionEvaluator implements ExpressionEvaluator
{
	private InspectionContext<?, ?>	inspectionContext;

	@Override
	public void setInspectionContext(InspectionContext<?, ?> inspectionContext) {
		this.inspectionContext = inspectionContext;
	}

	@Override
	public void evaluate(String expression, Object thisValue) {
		// TODO
	}
}
