package dd.kms.marple.components;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @param <C>	GUI component class
 * @param <P>	Point class
 */
class ComponentHierarchyModelBuilderImpl<C, P> implements ComponentHierarchyModelBuilder<C, P>
{
	private final Function<C, C> 												parentFunction;

	/*
	 * Since we want the user to add additional strategies without having to know which strategies
	 * are already defined, we cannot use ImmutableMap.Builder here. Otherwise, the user would get
	 * an exception if he specified a strategy for the same component class for which there is already
	 * a default strategy defined.
	 */
	private final Map<Class<? extends C>, SubcomponentHierarchyStrategy<C, P>>	subcomponentHierarchyStrategies	= new HashMap<>();

	ComponentHierarchyModelBuilderImpl(Function<C, C> parentFunction) {
		this.parentFunction = parentFunction;
	}

	@Override
	public <T extends C> ComponentHierarchyModelBuilder<C, P> subcomponentHierarchyStrategy(Class<T> componentClass, SubcomponentHierarchyStrategy<T, P> subcomponentHierarchyStrategy) {
		subcomponentHierarchyStrategies.put(
			componentClass,
			(component, p) -> componentClass.isInstance(component)
							? subcomponentHierarchyStrategy.getSubcomponentHierarchy(componentClass.cast(component), p)
							: ImmutableList.of()
		);
		return this;
	}

	@Override
	public ComponentHierarchyModel<C, P> build() {
		return new ComponentHierarchyModelImpl(parentFunction, subcomponentHierarchyStrategies);
	}
}
