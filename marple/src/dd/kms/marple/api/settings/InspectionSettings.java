package dd.kms.marple.api.settings;

import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchyModel;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.api.settings.visual.VisualSettings;

import javax.annotation.Nullable;
import java.nio.file.Path;

public interface InspectionSettings
{
	ObjectInspector getInspector();
	ExpressionEvaluator getEvaluator();
	ComponentHierarchyModel getComponentHierarchyModel();
	EvaluationSettings getEvaluationSettings();
	VisualSettings getVisualSettings();
	CustomActionSettings getCustomActionSettings();
	SecuritySettings getSecuritySettings();
	KeySettings getKeySettings();
	@Nullable
	Path getPreferencesFile();
}
