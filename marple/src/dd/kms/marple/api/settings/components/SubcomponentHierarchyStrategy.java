package dd.kms.marple.api.settings.components;

import java.awt.*;
import java.util.List;

@FunctionalInterface
public interface SubcomponentHierarchyStrategy<C extends Component>
{
	List<?> getSubcomponentHierarchy(C component, Point point);
}
