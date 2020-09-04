package dd.kms.marple.gui.inspector.views.mapview.settings;

public class FilterSettings implements OperationSettings
{
	private final String	keyFilterExpression;
	private final String	valueFilterExpression;

	public FilterSettings(String keyFilterExpression, String valueFilterExpression) {
		this.keyFilterExpression = keyFilterExpression;
		this.valueFilterExpression = valueFilterExpression;
	}

	@Override
	public Operation getOperation() {
		return Operation.FILTER;
	}

	public String getKeyFilterExpression() {
		return keyFilterExpression;
	}

	public String getValueFilterExpression() {
		return valueFilterExpression;
	}
}
