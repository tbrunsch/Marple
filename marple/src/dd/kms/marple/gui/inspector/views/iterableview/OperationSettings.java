package dd.kms.marple.gui.inspector.views.iterableview;

class OperationSettings
{
	private final Operation				operation;
	private final OperationResultType	resultType;
	private final String				expression;

	OperationSettings(Operation operation, OperationResultType resultType, String expression) {
		this.operation = operation;
		this.resultType = resultType;
		this.expression = expression;
	}

	Operation getOperation() {
		return operation;
	}

	OperationResultType getResultType() {
		return resultType;
	}

	String getExpression() {
		return expression;
	}
}
