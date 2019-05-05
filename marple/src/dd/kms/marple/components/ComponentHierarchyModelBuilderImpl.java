package dd.kms.marple.components;

import com.google.common.collect.ImmutableList;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

class ComponentHierarchyModelBuilderImpl implements ComponentHierarchyModelBuilder
{
	/*
	 * Since we want the user to add additional strategies without having to know which strategies
	 * are already defined, we cannot use ImmutableMap.Builder here. Otherwise, the user would get
	 * an exception if he specified a strategy for the same component class for which there is already
	 * a default strategy defined.
	 */
	private final Map<Class<? extends Component>, SubcomponentHierarchyStrategy<Component>>	subcomponentHierarchyStrategies	= new HashMap<>();

	@Override
	public <C extends Component> ComponentHierarchyModelBuilder subcomponentHierarchyStrategy(Class<C> componentClass, SubcomponentHierarchyStrategy<C> subcomponentHierarchyStrategy) {
		subcomponentHierarchyStrategies.put(
			componentClass,
			(component, p) -> componentClass.isInstance(component)
							? subcomponentHierarchyStrategy.getSubcomponentHierarchy(componentClass.cast(component), p)
							: ImmutableList.of()
		);
		return this;
	}

	@Override
	public ComponentHierarchyModel build() {
		return new ComponentHierarchyModelImpl(subcomponentHierarchyStrategies);
	}
}
