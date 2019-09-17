package dd.kms.marple.gui.inspector.views.iterableview;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.CompiledExpression;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

class ForEachOperationExecutor extends AbstractOperationExecutor
{
	ForEachOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, InspectionContext inspectionContext) {
		super(iterable, commonElementType, inspectionContext);
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
				compiledExpression.evaluate(InfoProvider.createObjectInfo(element));
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
			count++;
		}
		displayText("Performed operation for all " + count + " elements");
	}
}
