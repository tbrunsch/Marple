package dd.kms.marple.components;

import java.awt.*;

public interface ComponentHierarchyModelBuilder
{
	<C extends Component> ComponentHierarchyModelBuilder subcomponentHierarchyStrategy(Class<C> componentClass, SubcomponentHierarchyStrategy<C> subcomponentHierarchyStrategy);
	ComponentHierarchyModel build();
}
