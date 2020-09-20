package dd.kms.marple.api.settings.visual;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.util.List;

public interface VisualSettings
{
	String getDisplayText(ObjectInfo objectInfo);
	List<ObjectView> getInspectionViews(ObjectInfo objectInfo, InspectionContext context);
}
