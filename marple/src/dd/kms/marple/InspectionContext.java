package dd.kms.marple;

import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.settings.InspectionSettings;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public interface InspectionContext
{
	InspectionSettings getSettings();

	InspectionAction createHistoryBackAction();
	InspectionAction createHistoryForwardAction();
	InspectionAction createInspectComponentAction(ComponentHierarchy componentHierarchy);
	InspectionAction createInspectObjectAction(ObjectInfo objectInfo);
	InspectionAction createAddVariableAction(String suggestedName, ObjectInfo value);
	InspectionAction createEvaluateExpressionAction(String expression, ObjectInfo thisValue);
	InspectionAction createEvaluateExpressionAction(String expression, ObjectInfo thisValue, int caretPosition);
	InspectionAction createEvaluateAsThisAction(ObjectInfo thisValue);
	InspectionAction createSearchInstancesFromHereAction(ObjectInfo root);
	InspectionAction createSearchInstanceAction(ObjectInfo target);
	InspectionAction createDebugSupportAction(ObjectInfo thisValue);
	<T> InspectionAction createSnapshotAction(T snapshotTarget, Function<T, BufferedImage> snapshotFunction);

	void clearHistory();

	String getDisplayText(ObjectInfo objectInfo);
	List<ObjectView> getInspectionViews(ObjectInfo objectInfo);
	ExpressionEvaluator getEvaluator();
}
