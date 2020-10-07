package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.collect.*;
import com.google.common.primitives.Primitives;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.GroupSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.MapSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.util.Objects;

class GroupOperationExecutor extends AbstractOperationExecutor<GroupSettings>
{
	GroupOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, GroupSettings settings, InspectionContext context) {
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
		Multimap<Object, Object> result = Comparable.class.isAssignableFrom(Primitives.wrap(resultClass))
										? MultimapBuilder.treeKeys(this::compare).linkedListValues().build()
										: ArrayListMultimap.create();
		for (Object element : iterable) {
			try {
				Object group = compiledExpression.evaluate(InfoProvider.createObjectInfo(element)).getObject();
				result.put(group, element);
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(InfoProvider.createObjectInfo(result));
	}

	private int compare(Object o1, Object o2) {
		if (Objects.equals(o1, o2)) {
			return 0;
		}
		return	o1 == null	? 1 :
				o2 == null	? -1
							: ((Comparable) o1).compareTo(o2);
	}
}
