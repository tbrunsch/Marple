package dd.kms.marple.api;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.api.settings.visual.UniformView;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public interface InspectionContext
{
	InspectionSettings getSettings();

	InspectionAction createInspectComponentAction(ComponentHierarchy componentHierarchy);
	InspectionAction createInspectObjectAction(Object object);
	InspectionAction createAddVariableAction(String suggestedName, Object value);
	InspectionAction createEvaluateExpressionAction(String expression, int caretPosition, Object thisValue);
	InspectionAction createSearchInstancesFromHereAction(Object root);
	InspectionAction createSearchInstanceAction(Object target);
	InspectionAction createDebugSupportAction(Object thisValue);
	InspectionAction createParameterizedCustomAction(CustomAction customAction, Object thisValue);
	<T> InspectionAction createSnapshotAction(T snapshotTarget, Function<T, BufferedImage> snapshotFunction);

	String getDisplayText(Object object);
	UniformView getUniformView();
	List<ObjectView> getInspectionViews(Object object);
	ExpressionEvaluator getEvaluator();
}
