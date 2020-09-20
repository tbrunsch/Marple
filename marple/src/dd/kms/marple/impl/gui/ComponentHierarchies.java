package dd.kms.marple.impl.gui;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.components.ComponentHierarchy;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentHierarchies
{
	public static ComponentHierarchy getComponentHierarchy(Component component) {
		List<Object> components = getComponentWithParents(component);
		return getComponentHierarchy(components);
	}

	public static ComponentHierarchy getComponentHierarchy(Component component, Point position, InspectionContext context) {
		List<Object> components = getComponentWithParents(component);
		List<?> subcomponentHierarchy = context.getSettings().getComponentHierarchyModel().getSubcomponentHierarchy(component, position);
		components.addAll(subcomponentHierarchy);
		return getComponentHierarchy(components);
	}

	private static ComponentHierarchy getComponentHierarchy(List<Object> components) {
		return new dd.kms.marple.impl.settings.components.ComponentHierarchyImpl(components);
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
