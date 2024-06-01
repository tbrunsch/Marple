package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.inspector.ObjectInspector;

public class InspectObjectAction implements InspectionAction
{
	private final ObjectInspector	inspector;
	private final Object			object;
	private final String			objectDisplayText;

	public InspectObjectAction(ObjectInspector inspector, Object object, String objectDisplayText) {
		this.inspector = inspector;
		this.object = object;
		this.objectDisplayText = objectDisplayText;
	}

	@Override
	public String getName() {
		return "Inspect";
	}

	@Override
	public String getDescription() {
		return "Inspect object '" + objectDisplayText + "' in the inspection dialog";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		inspector.inspectObject(object);
	}
}
