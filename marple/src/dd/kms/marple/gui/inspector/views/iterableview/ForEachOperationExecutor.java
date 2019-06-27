package dd.kms.marple.gui.inspector.views.iterableview;

import com.google.common.primitives.Primitives;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.CompiledExpression;
import dd.kms.zenodot.ParseException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ForEachOperationExecutor extends AbstractOperationExecutor
{
	ForEachOperationExecutor(Iterable<?> iterable, Class<?> commonElementClass, InspectionContext inspectionContext) {
		super(iterable, commonElementClass, inspectionContext);
	}

	@Override
	void execute(String expression, OperationResultType resultType) throws Exception {
		forEach(expression);
	}

	private void forEach(String expression) throws Exception {
		CompiledExpression compiledExpression = compile(expression);
		int count = 0;
		for (Object element : iterable) {
			try {
				compiledExpression.evaluate(element);
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
			count++;
		}
		displayText("Performed operation for all " + count + " elements");
	}
}
