package dd.kms.marple.impl.gui.inspector.views.mapview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.MapSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.util.LinkedHashMap;
import java.util.Map;

class MapOperationExecutor extends AbstractOperationExecutor<MapSettings>
{
	MapOperationExecutor(Map<?, ?> map, TypeInfo commonKeyType, TypeInfo commonValueType, MapSettings settings, InspectionContext context) {
		super(map, commonKeyType, commonValueType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String keyMappingExpression = settings.getKeyMappingExpression();
		CompiledExpression compiledKeyExpression = compileKeyExpression(keyMappingExpression);
		checkMappingExpression(keyMappingExpression, compiledKeyExpression);

		String valueMappingExpression = settings.getValueMappingExpression();
		CompiledExpression compiledValueExpression = compileValueExpression(valueMappingExpression);
		checkMappingExpression(valueMappingExpression, compiledValueExpression);

		Map<Object, Object> result = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object key = entry.getKey();
			Object mappedKey;
			try {
				mappedKey = compiledKeyExpression.evaluate(InfoProvider.createObjectInfo(key)).getObject();
			} catch (Exception e) {
				throw wrapEvaluationException(e, key);
			}

			Object value = entry.getValue();
			Object mappedValue;
			try {
				mappedValue = compiledValueExpression.evaluate(InfoProvider.createObjectInfo(value)).getObject();
			} catch (Exception e) {
				throw wrapEvaluationException(e, value);
			}

			result.put(mappedKey, mappedValue);
		}
		displayResult(InfoProvider.createObjectInfo(result));
	}

	private void checkMappingExpression(String mappingExpression, CompiledExpression compiledExpression) throws ParseException {
		Class<?> resultClass = compiledExpression.getResultType().getRawType();
		if (Primitives.unwrap(resultClass) == void.class) {
			throw new ParseException(mappingExpression, mappingExpression.length(), "The mapping expression must evaluate to something different than void", null);
		}
	}
}
