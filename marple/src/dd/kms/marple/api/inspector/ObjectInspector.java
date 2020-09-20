package dd.kms.marple.api.inspector;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public interface ObjectInspector
{
	static ObjectInspector create() {
		return new dd.kms.marple.impl.inspector.ObjectInspectorImpl();
	}

	void setInspectionContext(InspectionContext context);
	void inspectComponent(ComponentHierarchy componentHierarchy);
	void inspectObject(ObjectInfo objectInfo);
}
