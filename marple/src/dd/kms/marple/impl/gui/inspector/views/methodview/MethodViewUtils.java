package dd.kms.marple.impl.gui.inspector.views.methodview;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.InvokeMethodAction;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class MethodViewUtils
{
	private final Object			object;
	private final InspectionContext	context;

	MethodViewUtils(Object object, InspectionContext context) {
		this.object = object;
		this.context = context;
	}

	ActionProvider getMethodActionProvider(Method method) {
		ImmutableList.Builder<InspectionAction> actionsBuilder = ImmutableList.builder();
		int numParameters = method.getParameterCount();
		if (numParameters == 0) {
			actionsBuilder.add(createInvokeMethodAction(method));
		}
		String methodName = method.getName();
		String parameterPlaceholder = IntStream.range(0, numParameters).mapToObj(i -> "").collect(Collectors.joining(", "));
		String expression = methodName + "(" + parameterPlaceholder + ")";
		int caretPosition = numParameters == 0 ? expression.length() : methodName.length() + 1;
		actionsBuilder.add(context.createEvaluateExpressionAction(expression, caretPosition, object));
		return ActionProvider.of(methodName, actionsBuilder.build(), true);
	}

	private InspectionAction createInvokeMethodAction(Method method) {
		return new InvokeMethodAction(object, method, this::handleMethodReturnValue, this::displayMethodInvokationException);
	}

	private void handleMethodReturnValue(Object returnValue) {
		if (returnValue == null) {
			JOptionPane.showMessageDialog(null, "null");
			return;
		}
		InspectionAction inspectObjectAction = context.createInspectObjectAction(returnValue);
		inspectObjectAction.perform();
	}

	private void displayMethodInvokationException(Exception e) {
		// TODO: Better handling
		JOptionPane.showMessageDialog(null, "Caught the following exception:\n\n" + e.getMessage());
	}

	static String formatArguments(Method method, InspectionContext context) {
		int numParameters = method.getParameterCount();
		List<String> parameterTypeNames = new ArrayList<>(numParameters);
		Class<?>[] parameterTypes = method.getParameterTypes();

		for(int i = 0; i < numParameters; ++i) {
			Class<?> parameterType = parameterTypes[i];
			String argumentTypeName = i >= numParameters - 1 && method.isVarArgs()
				? context.getDisplayText(parameterType.getComponentType()) + "..."
				: context.getDisplayText(parameterType);
			parameterTypeNames.add(argumentTypeName);
		}

		return Joiner.on(", ").join(parameterTypeNames);
	}
}
