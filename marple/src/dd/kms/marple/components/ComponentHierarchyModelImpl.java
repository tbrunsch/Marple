package dd.kms.marple.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
		if (component == null) {
			return ImmutableList.of();
		}
		Class<? extends C> bestClass = null;
		List<? extends Object> bestSubcomponentHierarchy = Collections.emptyList();
		for (Class<? extends C> componentClass : subcomponentHierarchyStrategies.keySet()) {
			if (!componentClass.isInstance(component)) {
				continue;
			}
			SubcomponentHierarchyStrategy<C, P> subcomponentHierarchyStrategy = subcomponentHierarchyStrategies.get(componentClass);
			List<?> subcomponentHierarchy = subcomponentHierarchyStrategy.getSubcomponentHierarchy(component, position);
			if (subcomponentHierarchy.isEmpty()) {
				continue;
			}
			if (bestClass == null || bestClass.isAssignableFrom(componentClass)) {
				bestClass = componentClass;
				bestSubcomponentHierarchy = ImmutableList.copyOf(subcomponentHierarchy);
			}
		}
		return bestSubcomponentHierarchy;
	}
}
