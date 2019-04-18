package dd.kms.marple;

import dd.kms.marple.actions.InspectionAction;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @param <C>	GUI component class
 * @param <V>	View class (GUI component class plus name)
 */
public interface InspectionContext<C, V>
{
	InspectionAction createHistoryBackAction();
	InspectionAction createHistoryForwardAction();
	InspectionAction createInspectComponentAction(List<C> componentHierarchy, List<?> subcomponentHierarchy);
	InspectionAction createInspectObjectAction(Object object);
	InspectionAction createHighlightComponentAction(C component);
	InspectionAction createInvokeMethodAction(Object object, Method method, Consumer<Object> returnValueConsumer, Consumer<Exception> exceptionConsumer);
	InspectionAction createEvaluateExpressionAction(String expression, Object thisValue);
	InspectionAction createEvaluateAsThisAction(Object thisValue);
	String getDisplayText(Object object);
	List<V> getInspectionViews(Object object);
	void clearHistory();
}
