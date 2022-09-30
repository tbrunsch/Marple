package dd.kms.marple.impl.gui.inspector.views.mapview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.FilterSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

class FilterOperationExecutor extends AbstractOperationExecutor<FilterSettings>
{
	FilterOperationExecutor(Map<?, ?> map, Class<?> commonKeyType, Class<?> commonValueType, FilterSettings settings, InspectionContext context) {
		super(map, commonKeyType, commonValueType, settings, context);
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
		displayResult(result);
	}

	private void checkFilterExpression(String filterExpression, CompiledExpression compiledExpression) throws ParseException {
		Class<?> resultClass = compiledExpression.getResultType();
		if (Primitives.unwrap(resultClass) != boolean.class) {
			throw new ParseException(filterExpression, filterExpression.length(), "The filter expression must be a predicate", null);
		}
	}

	private boolean filter(CompiledExpression compiledFilterExpression, Object element) throws Exception {
		Object result = compiledFilterExpression.evaluate(element);
		if (result == null) {
			throw new NullPointerException();
		}
		Class<?> resultClass = result.getClass();
		if (Primitives.unwrap(resultClass) != boolean.class) {
			String error = MessageFormat.format(
				"Predicate does not yield a boolean, but an element of type {0} for element {1}",
				resultClass.getName(),
				context.getDisplayText(element)
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
