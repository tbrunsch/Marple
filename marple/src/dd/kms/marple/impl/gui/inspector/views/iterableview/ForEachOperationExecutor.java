package dd.kms.marple.impl.gui.inspector.views.iterableview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.ForEachSettings;
import dd.kms.zenodot.api.CompiledLambdaExpression;

import java.util.function.Consumer;

public class ForEachOperationExecutor extends AbstractOperationExecutor<ForEachSettings>
{
	public static final Class<Consumer>	FUNCTIONAL_INTERFACE	= Consumer.class;

	ForEachOperationExecutor(Object object, Iterable<?> iterable, Class<?> commonElementType, ForEachSettings settings, InspectionContext context) {
		super(object, iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String consumerExpression = settings.getConsumerExpression();
		CompiledLambdaExpression<Consumer> compiledExpression = compile(consumerExpression, FUNCTIONAL_INTERFACE, commonElementType);
		Consumer<Object> consumer = compiledExpression.evaluate(object);
		int count = 0;
		for (Object element : iterableView) {
			try {
				consumer.accept(element);
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
			count++;
		}
		displayText("Performed operation for all " + count + " elements");
	}
}
