package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.ToMapSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;

import java.util.LinkedHashMap;
import java.util.Map;

class ToMapOperationExecutor extends AbstractOperationExecutor<ToMapSettings>
{
	ToMapOperationExecutor(Iterable<?> iterable, Class<?> commonElementType, ToMapSettings settings, InspectionContext context) {
		super(iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String keyMappingExpression = settings.getKeyMappingExpression();
		String valueMappingExpression = settings.getValueMappingExpression();
		CompiledExpression compiledKeyMappingExpression = compile(keyMappingExpression);
		CompiledExpression compiledValueMappingExpression = compile(valueMappingExpression);

		Class<?> keyResultClass = compiledKeyMappingExpression.getResultType();
		if (Primitives.unwrap(keyResultClass) == void.class) {
			throw new ParseException(keyMappingExpression, keyMappingExpression.length(), "The key mapping expression must evaluate to something different than void", null);
		}

		Class<?> valueResultClass = compiledValueMappingExpression.getResultType();
		if (Primitives.unwrap(valueResultClass) == void.class) {
			throw new ParseException(valueMappingExpression, valueMappingExpression.length(), "The value mapping expression must evaluate to something different than void", null);
		}

		Map<Object, Object> result = new LinkedHashMap<>();
		for (Object element : iterable) {
			try {
				Object key = compiledKeyMappingExpression.evaluate(element);
				Object value = compiledValueMappingExpression.evaluate(element);
				result.put(key, value);
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(result);
	}
}
