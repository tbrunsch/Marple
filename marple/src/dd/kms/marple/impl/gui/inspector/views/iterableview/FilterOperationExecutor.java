package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.FilterResultType;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.FilterSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class FilterOperationExecutor extends AbstractOperationExecutor<FilterSettings>
{
	FilterOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, FilterSettings settings, InspectionContext context) {
		super(iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String filterExpression = settings.getFilterExpression();
		CompiledExpression compiledExpression = compile(filterExpression);
		Class<?> resultClass = compiledExpression.getResultType().getRawType();
		if (Primitives.unwrap(resultClass) != boolean.class) {
			throw new ParseException(filterExpression, filterExpression.length(), "The filter expression must be a predicate", null);
		}
		PredicateWithException filter = o -> filter(compiledExpression, o);
		final Object filterResult;
		FilterResultType resultType = settings.getResultType();
		switch (resultType) {
			case LIST:
				filterResult = filterToList(filter);
				break;
			case INDEX_MAP:
				filterResult = filterToIndexMap(filter);
				break;
			default:
				throw new IllegalArgumentException("Unsupported operation result type: " + resultType);
		}
		displayResult(InfoProvider.createObjectInfo(filterResult));
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
				context.getDisplayText(InfoProvider.createObjectInfo(element))
			);
			throw new IllegalStateException(error);
		}
		return (Boolean) result;
	}

	private List<?> filterToList(PredicateWithException filter) throws Exception {
		List<Object> result = new ArrayList<>();
		for (Object element : iterable) {
			try {
				if (filter.test(element)) {
					result.add(element);
				}
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		return result;
	}

	private Map<Integer, ?> filterToIndexMap(PredicateWithException filter) throws Exception {
		Map<Integer, Object> result = new LinkedHashMap<>();
		int index = 0;
		for (Object element : iterable) {
			try {
				if (filter.test(element)) {
					result.put(index, element);
				}
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
			index++;
		}
		return result;
	}

	@FunctionalInterface
	private interface PredicateWithException
	{
		boolean test(Object o) throws Exception;
	}
}
