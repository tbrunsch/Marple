package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public class InspectObjectAction implements InspectionAction
{
	private final ObjectInspector	inspector;
	private final ObjectInfo		objectInfo;
	private final String			objectDisplayText;

	public InspectObjectAction(ObjectInspector inspector, ObjectInfo objectInfo, String objectDisplayText) {
		this.inspector = inspector;
		this.objectInfo = objectInfo;
		this.objectDisplayText = objectDisplayText;
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
		return "Inspect object '" + objectDisplayText + "' in the inspection dialog";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		inspector.inspectObject(objectInfo);
	}
}
