package dd.kms.marple.impl.gui.inspector.views.iterableview.settings;

public class GroupSettings implements OperationSettings
{
	private final String	mappingExpression;

	public GroupSettings(String mappingExpression) {
		this.mappingExpression = mappingExpression;
	}

	@Override
	public Operation getOperation() {
		return Operation.GROUP;
	}

	public String getMappingExpression() {
		return mappingExpression;
	}
}
