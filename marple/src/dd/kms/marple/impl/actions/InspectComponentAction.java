package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.impl.settings.components.ComponentHierarchyImpl;

public class InspectComponentAction implements InspectionAction
{
	private final ObjectInspector		inspector;
	private final ComponentHierarchy	componentHierarchy;
	private final String				componentDisplayText;

	public InspectComponentAction(ObjectInspector inspector, ComponentHierarchy componentHierarchy, String componentDisplayText) {
		this.inspector = inspector;
		this.componentHierarchy = new ComponentHierarchyImpl(componentHierarchy);
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
		return "Inspect the (sub)component hierarchy of '" + componentDisplayText + "' in the inspection dialog";
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
