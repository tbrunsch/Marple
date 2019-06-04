package dd.kms.marple;

import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.gui.ObjectView;
import dd.kms.marple.settings.InspectionSettings;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

public interface InspectionContext
{
	InspectionSettings getSettings();

	InspectionAction createHistoryBackAction();
	InspectionAction createHistoryForwardAction();
	InspectionAction createInspectComponentAction(List<Component> componentHierarchy, List<?> subcomponentHierarchy);
	InspectionAction createInspectObjectAction(Object object);
	InspectionAction createHighlightComponentAction(Component component);
	InspectionAction createInvokeMethodAction(Object object, Method method, Consumer<Object> returnValueConsumer, Consumer<Exception> exceptionConsumer);
	InspectionAction createAddVariableAction(String suggestedName, Object value);
	InspectionAction createEvaluateExpressionAction(String expression, Object thisValue);
	InspectionAction createEvaluateExpressionAction(String expression, Object thisValue, int caretPosition);
	InspectionAction createEvaluateAsThisAction(Object thisValue);
	InspectionAction createSearchInstancesFromHereAction(Object root);

	void clearHistory();

	String getDisplayText(Object object);
	List<ObjectView> getInspectionViews(Object object);
	ExpressionEvaluator getEvaluator();
}
