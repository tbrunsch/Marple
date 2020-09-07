package dd.kms.marple.gui.inspector.views.iterableview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.inspector.views.iterableview.settings.MapSettings;
import dd.kms.marple.gui.inspector.views.iterableview.settings.ToMapSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ToMapOperationExecutor extends AbstractOperationExecutor<ToMapSettings>
{
	ToMapOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, ToMapSettings settings, InspectionContext inspectionContext) {
		super(iterable, commonElementType, settings, inspectionContext);
	}

	@Override
	void execute() throws Exception {
		String keyMappingExpression = settings.getKeyMappingExpression();
		String valueMappingExpression = settings.getValueMappingExpression();
		CompiledExpression compiledKeyMappingExpression = compile(keyMappingExpression);
		CompiledExpression compiledValueMappingExpression = compile(valueMappingExpression);

		Class<?> keyResultClass = compiledKeyMappingExpression.getResultType().getRawType();
		if (Primitives.unwrap(keyResultClass) == void.class) {
			throw new ParseException(keyMappingExpression, keyMappingExpression.length(), "The key mapping expression must evaluate to something different than void", null);
		}

		Class<?> valueResultClass = compiledValueMappingExpression.getResultType().getRawType();
		if (Primitives.unwrap(valueResultClass) == void.class) {
			throw new ParseException(valueMappingExpression, valueMappingExpression.length(), "The value mapping expression must evaluate to something different than void", null);
		}

		Map<Object, Object> result = new LinkedHashMap<>();
		for (Object element : iterable) {
			try {
				Object key = compiledKeyMappingExpression.evaluate(InfoProvider.createObjectInfo(element)).getObject();
				Object value = compiledValueMappingExpression.evaluate(InfoProvider.createObjectInfo(element)).getObject();
				result.put(key, value);
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(InfoProvider.createObjectInfo(result));
	}
}
