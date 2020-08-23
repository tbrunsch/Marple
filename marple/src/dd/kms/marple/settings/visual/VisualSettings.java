package dd.kms.marple.settings.visual;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.util.List;

public interface VisualSettings
{
	String getDisplayText(ObjectInfo objectInfo);
	List<ObjectView> getInspectionViews(ObjectInfo objectInfo, InspectionContext inspectionContext);
}
