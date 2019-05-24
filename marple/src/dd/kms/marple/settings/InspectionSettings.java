package dd.kms.marple.settings;

import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.inspector.ObjectInspector;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.gui.VisualSettings;

import java.awt.*;
import java.util.Optional;
import java.util.function.Predicate;

public interface InspectionSettings
{
	ObjectInspector getInspector();
	ExpressionEvaluator getEvaluator();
	ComponentHierarchyModel getComponentHierarchyModel();
	VisualSettings getVisualSettings();
	Predicate<Component> getResponsibilityPredicate();
	Optional<SecuritySettings> getSecuritySettings();

	KeyRepresentation getInspectionKey();
	KeyRepresentation getEvaluationKey();
	KeyRepresentation getSearchKey();
	KeyRepresentation getCodeCompletionKey();
	KeyRepresentation getShowMethodArgumentsKey();
}
