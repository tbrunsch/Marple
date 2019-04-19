package dd.kms.marple.actions;

import dd.kms.marple.ObjectInspector;

public class HighlightComponentAction<C> implements InspectionAction
{
	private final ObjectInspector<C> inspector;
	private final C					component;
	private final String			displayText;

	public HighlightComponentAction(ObjectInspector<C> inspector, C component, String displayText) {
		this.inspector = inspector;
		this.component = component;
		this.displayText = displayText;
	}

	@Override
	public String getName() {
		return "Highlight component '" + displayText + "'";
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
