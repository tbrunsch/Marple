package dd.kms.marple.impl.gui.inspector.views.iterableview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.ToMapSettings;
import dd.kms.zenodot.api.CompiledLambdaExpression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ToMapOperationExecutor extends AbstractOperationExecutor<ToMapSettings>
{
	public static final Class<Function>	FUNCTIONAL_INTERFACE_KEYS	= Function.class;
	public static final Class<Function>	FUNCTIONAL_INTERFACE_VALUES	= Function.class;

	ToMapOperationExecutor(Object object, Iterable<?> iterable, Class<?> commonElementType, ToMapSettings settings, InspectionContext context) {
		super(object, iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String keyMappingExpression = settings.getKeyMappingExpression();
		String valueMappingExpression = settings.getValueMappingExpression();
		CompiledLambdaExpression<Function> compiledKeyMappingExpression = compile(keyMappingExpression, FUNCTIONAL_INTERFACE_KEYS, commonElementType);
		CompiledLambdaExpression<Function> compiledValueMappingExpression = compile(valueMappingExpression, FUNCTIONAL_INTERFACE_VALUES, commonElementType);

		Function<Object, Object> keyMapping = compiledKeyMappingExpression.evaluate(object);
		Function<Object, Object> valueMapping = compiledValueMappingExpression.evaluate(object);

		Map<Object, Object> result = new LinkedHashMap<>();
		for (Object element : iterableView) {
			try {
				Object key = keyMapping.apply(element);
				Object value = valueMapping.apply(element);
				result.put(key, value);
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(result);
	}
}
