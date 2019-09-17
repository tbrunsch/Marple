package dd.kms.marple.gui.inspector.views.iterableview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.CompiledExpression;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class FilterOperationExecutor extends AbstractOperationExecutor
{
	FilterOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, InspectionContext inspectionContext) {
		super(iterable, commonElementType, inspectionContext);
	}

	@Override
	void execute(String expression, OperationResultType resultType) throws Exception {
		filter(expression, resultType);
	}

	private void filter(String expression, OperationResultType resultType) throws Exception {
		CompiledExpression compiledExpression = compile(expression);
		Class<?> resultClass = compiledExpression.getResultType().getRawType();
		if (Primitives.unwrap(resultClass) != boolean.class) {
			throw new ParseException(expression.length(), "The expression must be a predicate");
		}
		PredicateWithException filter = o -> filter(compiledExpression, o);
		final Object filterResult;
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
		displayResult(filterResult);
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
