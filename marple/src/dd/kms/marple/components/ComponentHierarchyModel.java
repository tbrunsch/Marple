package dd.kms.marple.components;

import java.awt.*;
import java.util.List;

public interface ComponentHierarchyModel
{
	List<?> getSubcomponentHierarchy(Component component, Point position);
}
