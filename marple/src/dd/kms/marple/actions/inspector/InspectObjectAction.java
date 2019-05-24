package dd.kms.marple.actions.inspector;

import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.inspector.ObjectInspector;

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
		return "Inspect object '" + objectDisplayText + "' in the object inspector";
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
