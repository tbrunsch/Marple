package dd.kms.marple.actions.inspector;

import dd.kms.marple.ComponentHierarchy;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.inspector.ObjectInspector;

public class InspectComponentAction implements InspectionAction
{
	private final ObjectInspector		inspector;
	private final ComponentHierarchy	componentHierarchy;
	private final String				componentDisplayText;

	public InspectComponentAction(ObjectInspector inspector, ComponentHierarchy componentHierarchy, String componentDisplayText) {
		this.inspector = inspector;
		this.componentHierarchy = new ComponentHierarchy(componentHierarchy);
		this.componentDisplayText = componentDisplayText;
	}

	@Override
	public boolean isDefaultAction() {
		return true;
	}

	@Override
	public String getName() {
		return "Inspect";
	}

	@Override
	public String getDescription() {
		return "Inspect the (sub)component hierarchy of '" + componentDisplayText + "' in the object inspector";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		inspector.inspectComponent(componentHierarchy);
	}
}
