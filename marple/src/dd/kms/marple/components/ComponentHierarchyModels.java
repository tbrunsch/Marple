package dd.kms.marple.components;

import dd.kms.marple.ComponentHierarchy;
import dd.kms.marple.InspectionContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class ComponentHierarchyModels
{
	public static ComponentHierarchyModelBuilder createBuilder() {
		return new ComponentHierarchyModelBuilderImpl();
	}

	public static <C extends Component> SubcomponentHierarchyStrategy<C> createSingleSubcomponentStrategy(BiFunction<C, Point, Object> subcomponentDetectionStrategy) {
		return (component, point) -> {
			Object subcomponent = subcomponentDetectionStrategy.apply(component, point);
			return subcomponent == null ? Collections.emptyList() : Collections.singletonList(subcomponent);
		};
	}

	public static ComponentHierarchy getComponentHierarchy(Component component) {
		List<Object> components = getComponentWithParents(component);
		return new ComponentHierarchy(components);
	}

	public static ComponentHierarchy getComponentHierarchy(Component component, Point position, InspectionContext context) {
		List<Object> components = getComponentWithParents(component);
		List<?> subcomponentHierarchy = context.getSettings().getComponentHierarchyModel().getSubcomponentHierarchy(component, position);
		components.addAll(subcomponentHierarchy);
		return new ComponentHierarchy(components);
	}

	private static List<Object> getComponentWithParents(Component component) {
		List<Object> componentsWithParents = new ArrayList<>();
		for (Component curComponent = component; curComponent != null; curComponent = curComponent.getParent()) {
			componentsWithParents.add(curComponent);
		}
		Collections.reverse(componentsWithParents);
		return componentsWithParents;
	}
}
