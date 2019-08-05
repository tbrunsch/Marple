package dd.kms.marple;

import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.marple.settings.InspectionSettings;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public interface InspectionContext
{
	InspectionSettings getSettings();

	InspectionAction createHistoryBackAction();
	InspectionAction createHistoryForwardAction();
	InspectionAction createInspectComponentAction(List<Component> componentHierarchy, List<?> subcomponentHierarchy);
	InspectionAction createInspectObjectAction(Object object);
	InspectionAction createAddVariableAction(String suggestedName, Object value);
	InspectionAction createEvaluateExpressionAction(String expression, Object thisValue);
	InspectionAction createEvaluateExpressionAction(String expression, Object thisValue, int caretPosition);
	InspectionAction createEvaluateAsThisAction(Object thisValue);
	InspectionAction createSearchInstancesFromHereAction(Object root);
	InspectionAction createSearchInstanceAction(Object target);
	InspectionAction createDebugSupportAction(Object thisValue);
	<T> InspectionAction createSnapshotAction(T snapshotTarget, Function<T, BufferedImage> snapshotFunction);

	void clearHistory();

	String getDisplayText(Object object);
	List<ObjectView> getInspectionViews(Object object);
	ExpressionEvaluator getEvaluator();
}
