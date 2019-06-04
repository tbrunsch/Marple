package dd.kms.marple.actions;

import dd.kms.marple.inspector.ObjectInspector;

import java.awt.*;

public class HighlightComponentAction implements InspectionAction
{
	private final ObjectInspector	inspector;
	private final Component			component;
	private final String			displayText;

	public HighlightComponentAction(ObjectInspector inspector, Component component, String displayText) {
		this.inspector = inspector;
		this.component = component;
		this.displayText = displayText;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return "Highlight";
	}

	@Override
	public String getDescription() {
		return "Highlight the component hierarchy of '" + displayText + "'";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		inspector.highlightComponent(component);
	}
}
