package dd.kms.marple.api.settings.visual;

import dd.kms.marple.api.InspectionContext;

import java.util.List;

public interface VisualSettings
{
	String getDisplayText(Object object);
	UniformView getUniformView();
	List<ObjectView> getInspectionViews(Object object, InspectionContext context);
}
