package dd.kms.marple.impl.settings.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.settings.components.ComponentHierarchyModel;
import dd.kms.marple.api.settings.components.SubcomponentHierarchyStrategy;
import dd.kms.marple.impl.common.ReflectionUtils;

import java.awt.*;
import java.util.List;
import java.util.Map;

class ComponentHierarchyModelImpl implements ComponentHierarchyModel
{
	private final Map<Class<? extends Component>, SubcomponentHierarchyStrategy<Component>>	subcomponentHierarchyStrategies;

	ComponentHierarchyModelImpl(Map<Class<? extends Component>, SubcomponentHierarchyStrategy<Component>> subcomponentHierarchyStrategies) {
		this.subcomponentHierarchyStrategies = ImmutableMap.copyOf(subcomponentHierarchyStrategies);
	}

	@Override
	public List<?> getSubcomponentHierarchy(Component component, Point position) {
		if (component == null) {
			return ImmutableList.of();
		}
		Class<? extends Component> componentClass = ReflectionUtils.getBestMatchingClass(component, subcomponentHierarchyStrategies.keySet());
		if (componentClass == null) {
			return ImmutableList.of();
		}
		SubcomponentHierarchyStrategy<Component> subcomponentHierarchyStrategy = subcomponentHierarchyStrategies.get(componentClass);
		return subcomponentHierarchyStrategy.getSubcomponentHierarchy(component, position);
	}
}
