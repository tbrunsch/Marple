package dd.kms.marple.impl.gui.inspector.views.iterableview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.FilterResultType;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.FilterSettings;
import dd.kms.zenodot.api.CompiledLambdaExpression;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FilterOperationExecutor extends AbstractOperationExecutor<FilterSettings>
{
	public static final Class<Predicate>	FUNCTIONAL_INTERFACE	= Predicate.class;

	FilterOperationExecutor(Object object, Iterable<?> iterable, Class<?> commonElementType, FilterSettings settings, InspectionContext context) {
		super(object, iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String filterExpression = settings.getFilterExpression();
		CompiledLambdaExpression<Predicate> compiledExpression = compile(filterExpression, FUNCTIONAL_INTERFACE, commonElementType);
		Predicate<Object> filter = compiledExpression.evaluate(object);
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
		displayResult(filterResult);
	}

	private List<?> filterToList(Predicate<Object> filter) throws Exception {
		List<Object> result = new ArrayList<>();
		for (Object element : iterableView) {
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

	private Map<Integer, ?> filterToIndexMap(Predicate<Object> filter) throws Exception {
		Map<Integer, Object> result = new LinkedHashMap<>();
		int index = 0;
		for (Object element : iterableView) {
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
}
