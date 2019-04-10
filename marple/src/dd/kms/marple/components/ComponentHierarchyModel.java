package dd.kms.marple.components;

import javax.annotation.Nullable;
import java.util.List;

/**
 *
 * @param <C>	GUI component class
 * @param <P>	Point class
 */
public interface ComponentHierarchyModel<C, P>
{
	@Nullable C getParent(C component);
	List<?> getSubcomponentHierarchy(C component, P position);
}
