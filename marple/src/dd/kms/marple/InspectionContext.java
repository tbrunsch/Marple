package dd.kms.marple;

import dd.kms.marple.actions.InspectionAction;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @param <C>	GUI component class
 */
public interface InspectionContext<C>
{
	InspectionAction createHistoryBackAction();
	InspectionAction createHistoryForwardAction();
	InspectionAction createInspectComponentAction(List<C> componentHierarchy, List<?> subcomponentHierarchy);
	InspectionAction createInspectObjectAction(Object object);
	InspectionAction createInvokeMethodAction(Object object, Method method, Consumer<Object> returnValueConsumer, Consumer<Exception> exceptionConsumer);
	InspectionAction createEvaluateExpressionAction(String expression, Object thisValue);
	InspectionAction createEvaluateAsThisAction(Object thisValue);
	String getDisplayString(Object fieldValue);
	void clearHistory();
}
