package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.CountSettings;
import dd.kms.zenodot.api.CompiledLambdaExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class CountOperationExecutor extends AbstractOperationExecutor<CountSettings>
{
	public static final Class<Function>	FUNCTIONAL_INTERFACE	=Function.class;

	CountOperationExecutor(Object object, Iterable<?> iterable, Class<?> commonElementType, CountSettings settings, InspectionContext context) {
		super(object, iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String mappingExpression = settings.getMappingExpression();
		CompiledLambdaExpression<Function> compiledExpression = compile(mappingExpression, FUNCTIONAL_INTERFACE, commonElementType);
		Class<?> resultClass = compiledExpression.getLambdaResultType();
		Function<Object, Object> mapping = compiledExpression.evaluate(object);
		Map<Object, Integer> result = Comparable.class.isAssignableFrom(Primitives.wrap(resultClass))
										? new TreeMap<>()
										: new HashMap<>();
		for (Object element : iterableView) {
			try {
				Object group = mapping.apply(element);
				Integer count = result.get(group);
				if (count == null) {
					count = 0;
				}
				result.put(group, count + 1);
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(result);
	}
}
