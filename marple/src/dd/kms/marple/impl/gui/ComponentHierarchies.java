package dd.kms.marple.impl.gui;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.zenodot.api.common.GeneralizedField;

import java.awt.*;
import java.util.*;
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

	/**
	 * Returns for each object of the component hierarchy the fields of objects higher in the
	 * hierarchy (or of the object itself) that reference this object.
	 */
	public static Multimap<Object, GeneralizedField> findReferencingFields(ComponentHierarchy componentHierarchy) {
		Multimap<Object, GeneralizedField> fieldsByObject = ArrayListMultimap.create();
		Set<Object> objectsToFind = new HashSet<>();

		/*
		 * Traverse components from child to parent because usually children are members of parents and not vice versa
		 */
		for (Object hierarchyNode : Lists.reverse(componentHierarchy.getComponents())) {
			objectsToFind.add(hierarchyNode);
			fieldsByObject.putAll(ReflectionUtils.findFieldValues(hierarchyNode, objectsToFind));
		}
		return fieldsByObject;
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
