package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.CountSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class CountOperationExecutor extends AbstractOperationExecutor<CountSettings>
{
	CountOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, CountSettings settings, InspectionContext context) {
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
		Map<Object, Integer> result = Comparable.class.isAssignableFrom(Primitives.wrap(resultClass))
										? new TreeMap<>()
										: new HashMap<>();
		for (Object element : iterable) {
			try {
				Object group = compiledExpression.evaluate(InfoProvider.createObjectInfo(element)).getObject();
				Integer count = result.get(group);
				if (count == null) {
					count = 0;
				}
				result.put(group, count + 1);
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(InfoProvider.createObjectInfo(result));
	}
}
