package dd.kms.marple.impl.gui.inspector.views.iterableview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.MapSettings;
import dd.kms.zenodot.api.CompiledLambdaExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MapOperationExecutor extends AbstractOperationExecutor<MapSettings>
{
	public static final Class<Function>	FUNCTIONAL_INTERFACE	= Function.class;

	MapOperationExecutor(Object object, Iterable<?> iterable, Class<?> commonElementType, MapSettings settings, InspectionContext context) {
		super(object, iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String mappingExpression = settings.getMappingExpression();
		CompiledLambdaExpression<Function> compiledExpression = compile(mappingExpression, FUNCTIONAL_INTERFACE, commonElementType);
		Function<Object, Object> mapping = compiledExpression.evaluate(object);
		List<Object> result = new ArrayList<>();
		for (Object element : iterableView) {
			try {
				result.add(mapping.apply(element));
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(result);
	}
}
