package dd.kms.marple.settings;

import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.inspector.ObjectInspector;
import dd.kms.marple.settings.keys.KeySettings;
import dd.kms.marple.settings.visual.VisualSettings;

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
