package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.collect.Lists;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.UniformView;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.CollectSettings;
import dd.kms.zenodot.api.CompiledLambdaExpression;
import dd.kms.zenodot.api.ParseException;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class CollectOperationExecutor extends AbstractOperationExecutor<CollectSettings>
{
	public static final Class<Supplier>	FUNCTIONAL_INTERFACE	= Supplier.class;

	CollectOperationExecutor(Object object, Iterable<?> iterable, Class<?> commonElementType, CollectSettings settings, InspectionContext context) {
		super(object, iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String constructorExpression = settings.getConstructorExpression();
		CompiledLambdaExpression<Supplier> compiledExpression = compile(constructorExpression, FUNCTIONAL_INTERFACE);
		Class<?> resultClass = compiledExpression.getLambdaResultType();
		Supplier<Object> supplier = compiledExpression.evaluate(object);
		if (Collection.class.isAssignableFrom(resultClass)) {
			Collection<Object> collection = (Collection<Object>) supplier.get();
			collection.clear();
			for (Object element : iterableView) {
				try {
					collection.add(element);
				} catch (Exception e) {
					throw wrapEvaluationException(e, element);
				}
			}
			displayResult(collection);
		} else if (resultClass.isArray()) {
			Object array = supplier.get();
			List<?> list = UniformView.canViewAsList(iterableView)
				? UniformView.asList(iterableView)
				: Lists.newArrayList(iterableView);
			int length = list.size();
			if (Array.getLength(array) != length) {
				array = Array.newInstance(array.getClass().getComponentType(), length);
			}
			for (int i = 0; i < length; i++) {
				Object element = list.get(i);
				try {
					Array.set(array, i, element);
				} catch (Exception e) {
					throw wrapEvaluationException(e, element);
				}
			}
			displayResult(array);
		} else {
			throw new ParseException(constructorExpression, constructorExpression.length(), "The constructor expression must evaluate to a Collection or to an appropriate array", null);
		}
	}
}
