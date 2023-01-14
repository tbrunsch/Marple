package dd.kms.marple.impl.gui.inspector.views.mapview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.MapSettings;
import dd.kms.zenodot.api.CompiledLambdaExpression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class MapOperationExecutor extends AbstractOperationExecutor<MapSettings>
{
	public static final Class<Function>	FUNCTIONAL_INTERFACE_KEYS	= Function.class;
	public static final Class<Function>	FUNCTIONAL_INTERFACE_VALUES	= Function.class;

	MapOperationExecutor(Map<?, ?> map, Class<?> commonKeyType, Class<?> commonValueType, MapSettings settings, InspectionContext context) {
		super(map, commonKeyType, commonValueType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String keyMappingExpression = settings.getKeyMappingExpression();
		CompiledLambdaExpression<Function> compiledKeyExpression = compile(keyMappingExpression, FUNCTIONAL_INTERFACE_KEYS, commonKeyType);

		String valueMappingExpression = settings.getValueMappingExpression();
		CompiledLambdaExpression<Function> compiledValueExpression = compile(valueMappingExpression, FUNCTIONAL_INTERFACE_VALUES, commonValueType);

		Function<Object, Object> keyMapping = compiledKeyExpression.evaluate(map);
		Function<Object, Object> valueMapping = compiledValueExpression.evaluate(map);

		Map<Object, Object> result = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object key = entry.getKey();
			Object mappedKey;
			try {
				mappedKey = keyMapping.apply(key);
			} catch (Exception e) {
				throw wrapEvaluationException(e, key);
			}

			Object value = entry.getValue();
			Object mappedValue;
			try {
				mappedValue = valueMapping.apply(value);
			} catch (Exception e) {
				throw wrapEvaluationException(e, value);
			}

			result.put(mappedKey, mappedValue);
		}
		displayResult(result);
	}
}
