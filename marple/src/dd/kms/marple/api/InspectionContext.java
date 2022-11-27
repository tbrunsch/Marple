package dd.kms.marple.api;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.api.settings.visual.ObjectView;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public interface InspectionContext
{
	InspectionSettings getSettings();

	InspectionAction createInspectionHistoryBackAction();
	InspectionAction createInspectionHistoryForwardAction();
	InspectionAction createInspectComponentAction(ComponentHierarchy componentHierarchy);
	InspectionAction createInspectObjectAction(Object object);
	InspectionAction createAddVariableAction(String suggestedName, Object value);
	InspectionAction createEvaluateExpressionAction(String expression, int caretPosition, Object thisValue);
	InspectionAction createSearchInstancesFromHereAction(Object root);
	InspectionAction createSearchInstanceAction(Object target);
	InspectionAction createDebugSupportAction(Object thisValue);
	<T> InspectionAction createSnapshotAction(T snapshotTarget, Function<T, BufferedImage> snapshotFunction);

	void clearInspectionHistory();

	String getDisplayText(Object object);
	List<ObjectView> getInspectionViews(Object object);
	ExpressionEvaluator getEvaluator();
}
