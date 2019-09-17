package dd.kms.marple.inspector;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.awt.*;
import java.util.List;

public interface ObjectInspector
{
	void setInspectionContext(InspectionContext inspectionContext);
	void inspectComponent(List<Component> componentHierarchy, List<?> subcomponentHierarchy);
	void inspectObject(ObjectInfo objectInfo);
}
