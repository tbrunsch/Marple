package dd.kms.marple.gui.inspector.views.iterableview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.inspector.views.iterableview.settings.ForEachSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

class ForEachOperationExecutor extends AbstractOperationExecutor<ForEachSettings>
{
	ForEachOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, ForEachSettings settings, InspectionContext inspectionContext) {
		super(iterable, commonElementType, settings, inspectionContext);
	}

	@Override
	void execute() throws Exception {
		String consumerExpression = settings.getConsumerExpression();
		CompiledExpression compiledExpression = compile(consumerExpression);
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
