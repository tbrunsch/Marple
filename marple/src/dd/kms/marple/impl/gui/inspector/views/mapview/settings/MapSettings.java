package dd.kms.marple.impl.gui.inspector.views.mapview.settings;

public class MapSettings implements OperationSettings
{
	private final String	keyMappingExpression;
	private final String	valueMappingExpression;

	public MapSettings(String keyMappingExpression, String valueMappingExpression) {
		this.keyMappingExpression = keyMappingExpression;
		this.valueMappingExpression = valueMappingExpression;
	}

	@Override
	public Operation getOperation() {
		return Operation.MAP;
	}

	public String getKeyMappingExpression() {
		return keyMappingExpression;
	}

	public String getValueMappingExpression() {
		return valueMappingExpression;
	}
}
