package dd.kms.marple.api.settings.components;

import java.awt.*;

public interface ComponentHierarchyModelBuilder
{
	static ComponentHierarchyModelBuilder create() {
		return new dd.kms.marple.impl.settings.components.ComponentHierarchyModelBuilderImpl();
	}

	<C extends Component> ComponentHierarchyModelBuilder subcomponentHierarchyStrategy(Class<C> componentClass, SubcomponentHierarchyStrategy<C> subcomponentHierarchyStrategy);
	ComponentHierarchyModel build();
}
