package dd.kms.marple.components;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

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

	public static List<Component> getComponentHierarchy(Component component) {
		ImmutableList.Builder<Component> componentHierarchyBuilder = ImmutableList.builder();
		for (Component curComponent = component; curComponent != null; curComponent = curComponent.getParent()) {
			componentHierarchyBuilder.add(curComponent);
		}
		return componentHierarchyBuilder.build().reverse();
	}

	public static Object getHierarchyLeaf(Component component, Point position, InspectionContext context) {
		if (component == null) {
			return null;
		}
		List<Component> componentHierarchy = Arrays.asList(component);
		List<?> subcomponentHierarchy = context.getSettings().getComponentHierarchyModel().getSubcomponentHierarchy(component, position);
		return getHierarchyLeaf(componentHierarchy, subcomponentHierarchy);
	}

	public static Object getHierarchyLeaf(List<Component> componentHierarchy, List<?> subcomponentHierarchy) {
		List<?> lastNonEmptyList =	!subcomponentHierarchy.isEmpty()	? subcomponentHierarchy :
									!componentHierarchy.isEmpty()		? componentHierarchy
																		: null;
		return lastNonEmptyList == null ? null : lastNonEmptyList.get(lastNonEmptyList.size()-1);
	}
}
