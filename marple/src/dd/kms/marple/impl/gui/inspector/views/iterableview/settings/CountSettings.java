package dd.kms.marple.impl.gui.inspector.views.iterableview.settings;

public class CountSettings implements OperationSettings
{
	private final String	mappingExpression;

	public CountSettings(String mappingExpression) {
		this.mappingExpression = mappingExpression;
	}

	@Override
	public Operation getOperation() {
		return Operation.COUNT;
	}

	public String getMappingExpression() {
		return mappingExpression;
	}
}
