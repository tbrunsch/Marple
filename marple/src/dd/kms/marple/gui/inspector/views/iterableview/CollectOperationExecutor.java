package dd.kms.marple.gui.inspector.views.iterableview;

import com.google.common.collect.Lists;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.TypedObjectInfo;
import dd.kms.marple.common.UniformView;
import dd.kms.marple.gui.inspector.views.iterableview.settings.CollectSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class CollectOperationExecutor extends AbstractOperationExecutor<CollectSettings>
{
	CollectOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, CollectSettings settings, InspectionContext inspectionContext) {
		super(iterable, commonElementType, settings, inspectionContext);
	}

	@Override
	void execute() throws Exception {
		String constructorExpression = settings.getConstructorExpression();
		CompiledExpression compiledExpression = compile(constructorExpression);
		Class<?> resultClass = compiledExpression.getResultType().getRawType();
		if (Collection.class.isAssignableFrom(resultClass)) {
			ObjectInfo collectionInfo = compiledExpression.evaluate(InfoProvider.NULL_LITERAL);
			Collection<Object> collection = (Collection<Object>) collectionInfo.getObject();
			collection.clear();
			for (Object element : iterable) {
				try {
					collection.add(element);
				} catch (Exception e) {
					throw wrapEvaluationException(e, element);
				}
			}
			displayResult(collection);
		} else if (resultClass.isArray()) {
			ObjectInfo arrayInfo = compiledExpression.evaluate(InfoProvider.NULL_LITERAL);
			Object array = arrayInfo.getObject();
			ObjectInfo iterableInfo = InfoProvider.createObjectInfo(iterable);
			final List<?> list;
			if (UniformView.canViewAsList(iterableInfo)) {
				TypedObjectInfo<List<?>> listInfo = UniformView.asList(iterableInfo);
				list = listInfo.getObject();
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
