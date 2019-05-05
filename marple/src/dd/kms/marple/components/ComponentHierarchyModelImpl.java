package dd.kms.marple.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.common.ReflectionUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class ComponentHierarchyModelImpl implements ComponentHierarchyModel
{
	private final Map<Class<? extends Component>, SubcomponentHierarchyStrategy<Component>>	subcomponentHierarchyStrategies;

	ComponentHierarchyModelImpl(Map<Class<? extends Component>, SubcomponentHierarchyStrategy<Component>> subcomponentHierarchyStrategies) {
		this.subcomponentHierarchyStrategies = ImmutableMap.copyOf(subcomponentHierarchyStrategies);
	}

	@Override
	public List<?> getSubcomponentHierarchy(Component component, Point position) {
		Class<? extends Component> componentClass = ReflectionUtils.getBestMatchingClass(component, subcomponentHierarchyStrategies.keySet());
		if (componentClass == null) {
			return ImmutableList.of();
		}
		SubcomponentHierarchyStrategy<Component> subcomponentHierarchyStrategy = subcomponentHierarchyStrategies.get(componentClass);
		return subcomponentHierarchyStrategy.getSubcomponentHierarchy(component, position);
	}
}
