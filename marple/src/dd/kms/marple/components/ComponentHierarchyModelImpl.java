package dd.kms.marple.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.common.ReflectionUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @param <C>	GUI component class
 * @param <P>	Point class
 */
class ComponentHierarchyModelImpl<C, P> implements ComponentHierarchyModel<C, P>
{
	private final Function<C, C>												parentFunction;
	private final Map<Class<? extends C>, SubcomponentHierarchyStrategy<C, P>>	subcomponentHierarchyStrategies;

	ComponentHierarchyModelImpl(Function<C, C> parentFunction, Map<Class<? extends C>, SubcomponentHierarchyStrategy<C, P>> subcomponentHierarchyStrategies) {
		this.parentFunction = parentFunction;
		this.subcomponentHierarchyStrategies = ImmutableMap.copyOf(subcomponentHierarchyStrategies);
	}

	@Override
	public @Nullable C getParent(C component) {
		return component == null ? null : parentFunction.apply(component);
	}

	@Override
	public List<?> getSubcomponentHierarchy(C component, P position) {
		Class<? extends C> componentClass = ReflectionUtils.getBestMatchingClass(component, subcomponentHierarchyStrategies.keySet());
		if (componentClass == null) {
			return ImmutableList.of();
		}
		SubcomponentHierarchyStrategy<C, P> subcomponentHierarchyStrategy = subcomponentHierarchyStrategies.get(componentClass);
		return subcomponentHierarchyStrategy.getSubcomponentHierarchy(component, position);
	}
}
