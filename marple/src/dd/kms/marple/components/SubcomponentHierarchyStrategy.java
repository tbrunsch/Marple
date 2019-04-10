package dd.kms.marple.components;

import java.util.List;

/**
 *
 * @param <C>	concrete GUI component class
 * @param <P>	Point class
 */
@FunctionalInterface
public interface SubcomponentHierarchyStrategy<C, P>
{
	List<?> getSubcomponentHierarchy(C component, P point);
}
