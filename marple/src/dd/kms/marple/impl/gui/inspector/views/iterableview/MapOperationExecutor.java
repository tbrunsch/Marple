package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.MapSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.util.ArrayList;
import java.util.List;

class MapOperationExecutor extends AbstractOperationExecutor<MapSettings>
{
	MapOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, MapSettings settings, InspectionContext context) {
		super(iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String mappingExpression = settings.getMappingExpression();
		CompiledExpression compiledExpression = compile(mappingExpression);
		Class<?> resultClass = compiledExpression.getResultType().getRawType();
		if (Primitives.unwrap(resultClass) == void.class) {
			throw new ParseException(mappingExpression, mappingExpression.length(), "The mapping expression must evaluate to something different than void", null);
		}
		List<Object> result = new ArrayList<>();
		for (Object element : iterable) {
			try {
				result.add(compiledExpression.evaluate(InfoProvider.createObjectInfo(element)).getObject());
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(InfoProvider.createObjectInfo(result));
	}
}
