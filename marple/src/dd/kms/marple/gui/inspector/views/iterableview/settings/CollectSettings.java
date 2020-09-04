package dd.kms.marple.gui.inspector.views.iterableview.settings;

public class CollectSettings implements OperationSettings
{
	private final String	constructorExpression;

	public CollectSettings(String constructorExpression) {
		this.constructorExpression = constructorExpression;
	}

	@Override
	public Operation getOperation() {
		return Operation.COLLECT;
	}

	public String getConstructorExpression() {
		return constructorExpression;
	}
}
