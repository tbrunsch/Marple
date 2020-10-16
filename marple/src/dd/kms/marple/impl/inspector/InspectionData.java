package dd.kms.marple.impl.inspector;

import dd.kms.marple.api.actions.InspectionAction;

public class InspectionData
{
	private final InspectionAction	action;
	private final Object			viewSettings;

	public InspectionData(InspectionAction action, Object viewSettings) {
		this.action = action;
		this.viewSettings = viewSettings;
	}

	public InspectionAction getAction() {
		return action;
	}

	public Object getViewSettings() {
		return viewSettings;
	}

	public InspectionData replaceViewSettings(Object viewSettings) {
		return new InspectionData(this.action, viewSettings);
	}
}
