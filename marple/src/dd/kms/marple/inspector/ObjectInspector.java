package dd.kms.marple.inspector;

import dd.kms.marple.ComponentHierarchy;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

public interface ObjectInspector
{
	void setInspectionContext(InspectionContext inspectionContext);
	void inspectComponent(ComponentHierarchy componentHierarchy);
	void inspectObject(ObjectInfo objectInfo);
}
