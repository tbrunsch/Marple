package dd.kms.marple.components;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ComponentHierarchyModels
{
	/**
	 *
	 * @param <C>	GUI component class
	 * @param <P>	Point class
	 */
	public static <C, P> ComponentHierarchyModelBuilder<C, P> createBuilder(Function<C, C> parentFunction) {
		return new ComponentHierarchyModelBuilderImpl<>(parentFunction);
	}

	/**
	 *
	 * @param <C>	concrete GUI component class
	 * @param <P>	Point class
	 */
	public static <C, P> SubcomponentHierarchyStrategy<C, P> createSingleSubcomponentStrategy(BiFunction<C, P, Object> subcomponentDetectionStrategy) {
		return (component, point) -> {
			Object subcomponent = subcomponentDetectionStrategy.apply(component, point);
			return subcomponent == null ? Collections.emptyList() : Collections.singletonList(subcomponent);
		};
	}
}
