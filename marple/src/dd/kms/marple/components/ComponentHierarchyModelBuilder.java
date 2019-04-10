package dd.kms.marple.components;

/**
 *
 * @param <C>	GUI component class
 * @param <P>	Point class
 */
public interface ComponentHierarchyModelBuilder<C, P>
{
	<T extends C> ComponentHierarchyModelBuilder<C, P> subcomponentHierarchyStrategy(Class<T> componentClass, SubcomponentHierarchyStrategy<T, P> subcomponentHierarchyStrategy);
	ComponentHierarchyModel<C, P> build();
}
