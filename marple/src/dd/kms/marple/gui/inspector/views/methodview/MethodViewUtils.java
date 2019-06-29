package dd.kms.marple.gui.inspector.views.methodview;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.actions.InvokeMethodAction;
import dd.kms.zenodot.common.AccessModifier;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class MethodViewUtils
{
	private final Object			object;
	private final InspectionContext	inspectionContext;

	MethodViewUtils(Object object, InspectionContext inspectionContext) {
		this.object = object;
		this.inspectionContext = inspectionContext;
	}

	ActionProvider getMethodActionProvider(Method method) {
		ImmutableList.Builder<InspectionAction> actionsBuilder = ImmutableList.builder();
		int numArguments = method.getParameterTypes().length;
		if (numArguments == 0) {
			actionsBuilder.add(createInvokeMethodAction(method));
		}
		String methodName = method.getName();
		String argumentPlaceholder = IntStream.range(0, numArguments).mapToObj(i -> "").collect(Collectors.joining(", "));
		String expression = methodName + "(" + argumentPlaceholder + ")";
		int caretPosition = numArguments == 0 ? expression.length() : methodName.length() + 1;
		actionsBuilder.add(inspectionContext.createEvaluateExpressionAction(expression, object, caretPosition));
		return ActionProvider.of(methodName, actionsBuilder.build());
	}

	AccessModifier getAccessModifier(Method method) {
		int modifiers = method.getModifiers();
		return AccessModifier.getValue(modifiers);
	}

	private InspectionAction createInvokeMethodAction(Method method) {
		return new InvokeMethodAction(object, method, this::handleMethodReturnValue, this::displayMethodInvokationException);
	}

	private void handleMethodReturnValue(Object returnValue) {
		if (returnValue == null) {
			JOptionPane.showMessageDialog(null, "null");
			return;
		}
		InspectionAction inspectObjectAction = inspectionContext.createInspectObjectAction(returnValue);
		inspectObjectAction.perform();
	}

	private void displayMethodInvokationException(Exception e) {
		// TODO: Better handling
		JOptionPane.showMessageDialog(null, "Caught the following exception:\n\n" + e.getMessage());
	}

	String getArgumentsAsString(Method method) {
		return Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "));
	}
}
