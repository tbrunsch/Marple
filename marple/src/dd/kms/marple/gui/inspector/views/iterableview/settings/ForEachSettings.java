package dd.kms.marple.gui.inspector.views.iterableview.settings;

public class ForEachSettings implements OperationSettings
{
	private final String	consumerExpression;

	public ForEachSettings(String consumerExpression) {
		this.consumerExpression = consumerExpression;
	}

	@Override
	public Operation getOperation() {
		return Operation.FOR_EACH;
	}

	public String getConsumerExpression() {
		return consumerExpression;
	}
}
