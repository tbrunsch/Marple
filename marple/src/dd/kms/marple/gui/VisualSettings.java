package dd.kms.marple.gui;

import dd.kms.marple.InspectionContext;

import java.util.List;

public interface VisualSettings
{
	String getDisplayText(Object object);
	List<ObjectView> getInspectionViews(Object object, InspectionContext inspectionContext);
}
