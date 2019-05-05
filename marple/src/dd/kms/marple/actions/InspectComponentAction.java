package dd.kms.marple.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.inspector.ObjectInspector;

import java.awt.*;
import java.util.List;

public class InspectComponentAction implements InspectionAction
{
	private final ObjectInspector	inspector;
	private final List<Component>	componentHierarchy;
	private final List<?>			subcomponentHierarchy;
	private final String			leafDisplayText;

	public InspectComponentAction(ObjectInspector inspector, List<Component> componentHierarchy, List<?> subcomponentHierarchy, String leafDisplayText) {
		this.inspector = inspector;
		this.componentHierarchy = ImmutableList.copyOf(componentHierarchy);
		this.subcomponentHierarchy = ImmutableList.copyOf(subcomponentHierarchy);
		this.leafDisplayText = leafDisplayText;
	}

	@Override
	public String getName() {
		return "Inspect "
			+ (subcomponentHierarchy.isEmpty() ? "component" : "subcomponent")
			+ " '"
			+ leafDisplayText
			+ "'";
	}

	@Override
	public String getDescription() {
		return "Inspect the (sub)component hierarchy of '" + leafDisplayText + "' in the object inspector";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		inspector.inspectComponent(componentHierarchy, subcomponentHierarchy);
	}
}
