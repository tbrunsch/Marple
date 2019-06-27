package dd.kms.marple.gui.inspector.views.iterableview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.CompiledExpression;
import dd.kms.zenodot.ParseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class MapOperationExecutor extends AbstractOperationExecutor
{
	MapOperationExecutor(Iterable<?> iterable, Class<?> commonElementClass, InspectionContext inspectionContext) {
		super(iterable, commonElementClass, inspectionContext);
	}

	@Override
	void execute(String expression, OperationResultType resultType) throws Exception {
		map(expression);
	}

	private void map(String expression) throws Exception {
		CompiledExpression compiledExpression = compile(expression);
		Class<?> resultClass = compiledExpression.getResultType().getRawType();
		if (Primitives.unwrap(resultClass) == void.class) {
			throw new ParseException(expression.length(), "The expression must evaluate to something different than void");
		}
		List<Object> result = new ArrayList<>();
		for (Object element : iterable) {
			try {
				result.add(compiledExpression.evaluate(element));
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(result);
	}
}
