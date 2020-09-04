package dd.kms.marple.gui.inspector.views.iterableview.settings;

public class MapSettings implements OperationSettings
{
	private final String	mappingExpression;

	public MapSettings(String mappingExpression) {
		this.mappingExpression = mappingExpression;
	}

	@Override
	public Operation getOperation() {
		return Operation.MAP;
	}

	public String getMappingExpression() {
		return mappingExpression;
	}
}
