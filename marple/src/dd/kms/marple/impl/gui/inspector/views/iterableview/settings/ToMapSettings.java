package dd.kms.marple.impl.gui.inspector.views.iterableview.settings;

public class ToMapSettings implements OperationSettings
{
	private final String	keyMappingExpression;
	private final String	valueMappingExpression;

	public ToMapSettings(String keyMappingExpression, String valueMappingExpression) {
		this.keyMappingExpression = keyMappingExpression;
		this.valueMappingExpression = valueMappingExpression;
	}

	@Override
	public Operation getOperation() {
		return Operation.TO_MAP;
	}

	public String getKeyMappingExpression() {
		return keyMappingExpression;
	}

	public String getValueMappingExpression() {
		return valueMappingExpression;
	}
}
