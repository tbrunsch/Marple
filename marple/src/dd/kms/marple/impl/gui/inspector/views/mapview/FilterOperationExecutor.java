package dd.kms.marple.impl.gui.inspector.views.mapview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.FilterSettings;
import dd.kms.zenodot.api.CompiledLambdaExpression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class FilterOperationExecutor extends AbstractOperationExecutor<FilterSettings>
{
	public static final Class<Predicate>	FUNCTIONAL_INTERFACE_KEYS	= Predicate.class;
	public static final Class<Predicate>	FUNCTIONAL_INTERFACE_VALUES	= Predicate.class;

	FilterOperationExecutor(Map<?, ?> map, Class<?> commonKeyType, Class<?> commonValueType, FilterSettings settings, InspectionContext context) {
		super(map, commonKeyType, commonValueType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String keyFilterExpression = settings.getKeyFilterExpression();
		CompiledLambdaExpression<Predicate> compiledKeyExpression = compile(keyFilterExpression, FUNCTIONAL_INTERFACE_KEYS, commonKeyType);

		String valueFilterExpression = settings.getValueFilterExpression();
		CompiledLambdaExpression<Predicate> compiledValueExpression = compile(valueFilterExpression, FUNCTIONAL_INTERFACE_VALUES, commonValueType);

		Predicate<Object> keyFilter = compiledKeyExpression.evaluate(map);
		Predicate<Object> valueFilter = compiledValueExpression.evaluate(map);

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
}
