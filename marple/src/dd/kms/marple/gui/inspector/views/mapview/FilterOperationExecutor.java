package dd.kms.marple.gui.inspector.views.mapview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.inspector.views.mapview.settings.FilterSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

class FilterOperationExecutor extends AbstractOperationExecutor<FilterSettings>
{
	FilterOperationExecutor(Map<?, ?> map, TypeInfo commonKeyType, TypeInfo commonValueType, FilterSettings settings, InspectionContext inspectionContext) {
		super(map, commonKeyType, commonValueType, settings, inspectionContext);
	}

	@Override
	void execute() throws Exception {
		String keyFilterExpression = settings.getKeyFilterExpression();
		CompiledExpression compiledKeyExpression = compileKeyExpression(keyFilterExpression);
		checkFilterExpression(keyFilterExpression, compiledKeyExpression);

		String valueFilterExpression = settings.getValueFilterExpression();
		CompiledExpression compiledValueExpression = compileValueExpression(valueFilterExpression);
		checkFilterExpression(valueFilterExpression, compiledValueExpression);

		PredicateWithException keyFilter = o -> filter(compiledKeyExpression, o);
		PredicateWithException valueFilter = o -> filter(compiledValueExpression, o);

		Map<Object, Object> result = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object key = entry.getKey();
			try {
				if (!keyFilter.test(key)) {
					continue;
				}
			} catch (Exception e) {
				throw wrapEvaluationException(e, key);
			}
			Object value = entry.getValue();
			try {
				if (!valueFilter.test(value)) {
					continue;
				}
			} catch (Exception e) {
				throw wrapEvaluationException(e, value);
			}
			result.put(key, value);
		}
		displayResult(InfoProvider.createObjectInfo(result));
	}

	private void checkFilterExpression(String filterExpression, CompiledExpression compiledExpression) throws ParseException {
		Class<?> resultClass = compiledExpression.getResultType().getRawType();
		if (Primitives.unwrap(resultClass) != boolean.class) {
			throw new ParseException(filterExpression, filterExpression.length(), "The filter expression must be a predicate", null);
		}
	}

	private boolean filter(CompiledExpression compiledFilterExpression, Object element) throws Exception {
		Object result = compiledFilterExpression.evaluate(InfoProvider.createObjectInfo(element)).getObject();
		if (result == null) {
			throw new NullPointerException();
		}
		Class<?> resultClass = result.getClass();
		if (Primitives.unwrap(resultClass) != boolean.class) {
			String error = MessageFormat.format(
				"Predicate does not yield a boolean, but an element of type {0} for element {1}",
				resultClass.getName(),
				inspectionContext.getDisplayText(InfoProvider.createObjectInfo(element))
			);
			throw new IllegalStateException(error);
		}
		return (Boolean) result;
	}

	@FunctionalInterface
	private interface PredicateWithException
	{
		boolean test(Object o) throws Exception;
	}
}
