package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.primitives.Primitives;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.GroupSettings;
import dd.kms.zenodot.api.CompiledLambdaExpression;

import java.util.Objects;
import java.util.function.Function;

public class GroupOperationExecutor extends AbstractOperationExecutor<GroupSettings>
{
	public static final Class<Function>	FUNCTIONAL_INTERFACE	= Function.class;

	GroupOperationExecutor(Object object, Iterable<?> iterable, Class<?> commonElementType, GroupSettings settings, InspectionContext context) {
		super(object, iterable, commonElementType, settings, context);
	}

	@Override
	void execute() throws Exception {
		String mappingExpression = settings.getMappingExpression();
		CompiledLambdaExpression<Function> compiledExpression = compile(mappingExpression, FUNCTIONAL_INTERFACE, commonElementType);
		Class<?> resultClass = compiledExpression.getLambdaResultType();
		Function<Object, Object> mapping = compiledExpression.evaluate(object);
		Multimap<Object, Object> result = Comparable.class.isAssignableFrom(Primitives.wrap(resultClass))
										? MultimapBuilder.treeKeys(this::compare).linkedListValues().build()
										: ArrayListMultimap.create();
		for (Object element : iterableView) {
			try {
				Object group = mapping.apply(element);
				result.put(group, element);
			} catch (Exception e) {
				throw wrapEvaluationException(e, element);
			}
		}
		displayResult(result);
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
