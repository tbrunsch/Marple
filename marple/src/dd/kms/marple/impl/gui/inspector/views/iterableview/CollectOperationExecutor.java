package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.collect.Lists;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.UniformView;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.CollectSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

class CollectOperationExecutor extends AbstractOperationExecutor<CollectSettings>
{
	CollectOperationExecutor(Iterable<?> iterable, Class<?> commonElementType, CollectSettings settings, InspectionContext context) {
		super(iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String constructorExpression = settings.getConstructorExpression();
		CompiledExpression compiledExpression = compile(constructorExpression);
		Class<?> resultClass = compiledExpression.getResultType();
		if (Collection.class.isAssignableFrom(resultClass)) {
			Collection<Object> collection = (Collection<Object>) compiledExpression.evaluate(null);
			collection.clear();
			for (Object element : iterable) {
				try {
					collection.add(element);
				} catch (Exception e) {
					throw wrapEvaluationException(e, element);
				}
			}
			displayResult(compiledExpression.evaluate(null));
		} else if (resultClass.isArray()) {
			Object array = compiledExpression.evaluate(null);
			final List<?> list;
			if (UniformView.canViewAsList(iterable)) {
				list = UniformView.asList(iterable);
			} else {
				list = Lists.newArrayList(iterable);
			}
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
