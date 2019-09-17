package dd.kms.marple.gui.inspector.views.methodview;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.actions.InvokeMethodAction;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class MethodViewUtils
{
	private final ObjectInfo		objectInfo;
	private final InspectionContext	inspectionContext;

	MethodViewUtils(ObjectInfo objectInfo, InspectionContext inspectionContext) {
		this.objectInfo = objectInfo;
		this.inspectionContext = inspectionContext;
	}

	ActionProvider getMethodActionProvider(ExecutableInfo methodInfo) {
		ImmutableList.Builder<InspectionAction> actionsBuilder = ImmutableList.builder();
		int numArguments = methodInfo.getNumberOfArguments();
		if (numArguments == 0) {
			actionsBuilder.add(createInvokeMethodAction(methodInfo));
		}
		String methodName = methodInfo.getName();
		String argumentPlaceholder = IntStream.range(0, numArguments).mapToObj(i -> "").collect(Collectors.joining(", "));
		String expression = methodName + "(" + argumentPlaceholder + ")";
		int caretPosition = numArguments == 0 ? expression.length() : methodName.length() + 1;
		actionsBuilder.add(inspectionContext.createEvaluateExpressionAction(expression, objectInfo, caretPosition));
		return ActionProvider.of(methodName, actionsBuilder.build(), true);
	}

	private InspectionAction createInvokeMethodAction(ExecutableInfo methodInfo) {
		return new InvokeMethodAction(objectInfo, methodInfo, this::handleMethodReturnValue, this::displayMethodInvokationException);
	}

	private void handleMethodReturnValue(ObjectInfo returnValueInfo) {
		if (returnValueInfo.getObject() == null) {
			JOptionPane.showMessageDialog(null, "null");
			return;
		}
		InspectionAction inspectObjectAction = inspectionContext.createInspectObjectAction(returnValueInfo);
		inspectObjectAction.perform();
	}

	private void displayMethodInvokationException(Exception e) {
		// TODO: Better handling
		JOptionPane.showMessageDialog(null, "Caught the following exception:\n\n" + e.getMessage());
	}
}
