package dd.kms.marple.impl.gui.inspector.views.iterableview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.ForEachSettings;
import dd.kms.zenodot.api.CompiledExpression;

class ForEachOperationExecutor extends AbstractOperationExecutor<ForEachSettings>
{
	ForEachOperationExecutor(Iterable<?> iterable, Class<?> commonElementType, ForEachSettings settings, InspectionContext context) {
		super(iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String consumerExpression = settings.getConsumerExpression();
		CompiledExpression compiledExpression = compile(consumerExpression);
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
