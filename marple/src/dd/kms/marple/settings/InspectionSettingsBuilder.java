package dd.kms.marple.settings;

import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.inspector.ObjectInspector;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.gui.VisualSettings;

import java.awt.*;
import java.util.Optional;
import java.util.function.Predicate;

public interface InspectionSettingsBuilder
{
	InspectionSettingsBuilder inspector(ObjectInspector inspector);
	InspectionSettingsBuilder evaluator(ExpressionEvaluator evaluator);
	InspectionSettingsBuilder componentHierarchyModel(ComponentHierarchyModel componentHierarchyModel);
	InspectionSettingsBuilder visualSettings(VisualSettings visualSettings);
	InspectionSettingsBuilder responsibilityPredicate(Predicate<Component> responsibilityPredicate);
	InspectionSettingsBuilder securitySettings(Optional<SecuritySettings> securitySettings);
	InspectionSettingsBuilder inspectionKey(KeyRepresentation inspectionKey);
	InspectionSettingsBuilder evaluationKey(KeyRepresentation evaluationKey);
	InspectionSettingsBuilder searchKey(KeyRepresentation searchKey);
	InspectionSettingsBuilder codeCompletionKey(KeyRepresentation codeCompletionKey);
	InspectionSettingsBuilder showMethodArgumentsKey(KeyRepresentation showMethodArgumentsKey);
	InspectionSettings build();
}
