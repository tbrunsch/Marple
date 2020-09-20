package dd.kms.marple.api.settings;

import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.api.settings.components.ComponentHierarchyModel;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.api.settings.visual.VisualSettings;

public interface InspectionSettings
{
	ObjectInspector getInspector();
	ExpressionEvaluator getEvaluator();
	ComponentHierarchyModel getComponentHierarchyModel();
	VisualSettings getVisualSettings();
	SecuritySettings getSecuritySettings();
	DebugSettings getDebugSettings();
	KeySettings getKeySettings();
}
