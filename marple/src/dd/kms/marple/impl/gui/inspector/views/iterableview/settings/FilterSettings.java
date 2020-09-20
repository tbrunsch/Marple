package dd.kms.marple.impl.gui.inspector.views.iterableview.settings;

public class FilterSettings implements OperationSettings
{
	private final FilterResultType	resultType;
	private final String			filterExpression;

	public FilterSettings(FilterResultType resultType, String filterExpression) {
		this.resultType = resultType;
		this.filterExpression = filterExpression;
	}

	@Override
	public Operation getOperation() {
		return Operation.FILTER;
	}

	public FilterResultType getResultType() {
		return resultType;
	}

	public String getFilterExpression() {
		return filterExpression;
	}
}
