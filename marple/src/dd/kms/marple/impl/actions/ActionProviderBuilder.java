package dd.kms.marple.impl.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.gui.snapshot.Snapshots;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class ActionProviderBuilder
{
	private static final Pattern VARIABLE_NAME_PATTERN	= Pattern.compile("^[A-Za-z][_A-Za-z0-9]*");

	private final String					displayText;
	private final ObjectInfo				objectInfo;
	private final InspectionContext			context;

	private @Nullable ComponentHierarchy	componentHierarchy;
	private @Nullable String				suggestedVariableName;
	private @Nullable EvaluationData		evaluationData;
	private boolean							executeDefaultAction	= true;

	public ActionProviderBuilder(String displayText, ComponentHierarchy componentHierarchy, InspectionContext context) {
		this.displayText = displayText;
		this.objectInfo = InfoProvider.createObjectInfo(componentHierarchy.getSelectedComponent());
		this.context = context;

		this.componentHierarchy = componentHierarchy;
	}

	public ActionProviderBuilder(String displayText, ObjectInfo objectInfo, InspectionContext context) {
		this.displayText = displayText;
		this.objectInfo = objectInfo;
		this.context = context;
	}

	public ActionProviderBuilder suggestVariableName(String suggestedVariableName) {
		if (suggestedVariableName != null && VARIABLE_NAME_PATTERN.matcher(suggestedVariableName).matches()) {
			this.suggestedVariableName = suggestedVariableName;
		}
		return this;
	}

	public ActionProviderBuilder evaluateAs(String expression) {
		return evaluateAs(expression, InfoProvider.NULL_LITERAL);
	}

	public ActionProviderBuilder evaluateAs(String expression, ObjectInfo expressionContext) {
		this.evaluationData = new EvaluationData(expression, expressionContext);
		suggestVariableName(expression);
		return this;
	}

	public ActionProviderBuilder executeDefaultAction(boolean executeDefaultAction) {
		this.executeDefaultAction = executeDefaultAction;
		return this;
	}

	public ActionProvider build() {
		if (objectInfo == null) {
			return null;
		}
		ImmutableList.Builder<InspectionAction> actionsBuilder = ImmutableList.builder();
		Object object = objectInfo.getObject();
		if (object == null) {
			return ActionProvider.of(displayText, actionsBuilder.build(), executeDefaultAction);
		}
		boolean isInspectable = ReflectionUtils.isObjectInspectable(object);
		if (componentHierarchy == null) {
			if (isInspectable) {
				actionsBuilder.add(context.createInspectObjectAction(objectInfo));
			}
		} else {
			actionsBuilder.add(context.createInspectComponentAction(componentHierarchy));
		}
		if (object instanceof Component) {
			actionsBuilder.add(new HighlightComponentAction((Component) object));
		}
		if (object instanceof JComponent) {
			actionsBuilder.add(context.createSnapshotAction((JComponent) object, Snapshots::takeSnapshot));
		}
		if (object instanceof Image) {
			actionsBuilder.add(context.createSnapshotAction((Image) object, Snapshots::takeSnapshot));
		}
		if (object instanceof Icon) {
			actionsBuilder.add(context.createSnapshotAction((Icon) object, Snapshots::takeSnapshot));
		}
		if (object instanceof Paint) {
			actionsBuilder.add(context.createSnapshotAction((Paint) object, paint -> Snapshots.takeSnapshot(paint, 200, 200)));
		}
		actionsBuilder.add(context.createAddVariableAction(suggestedVariableName, objectInfo));
		actionsBuilder.add(context.createEvaluateAsThisAction(objectInfo));
		if (evaluationData != null) {
			String expression = evaluationData.getExpression();
			ObjectInfo expressionContext = evaluationData.getExpressionContext();
			actionsBuilder.add(context.createEvaluateExpressionAction(expression, expressionContext));
		}
		actionsBuilder.add(context.createSearchInstancesFromHereAction(objectInfo));
		actionsBuilder.add(context.createSearchInstanceAction(objectInfo));
		actionsBuilder.add(new CopyStringRepresentationAction(object));
		actionsBuilder.add(context.createDebugSupportAction(objectInfo));
		return ActionProvider.of(displayText, actionsBuilder.build(), executeDefaultAction);
	}

	private static class ComponentHierarchyData
	{
		private final List<Component>	componentHierarchy;
		private final List<?>			subcomponentHierarchy;

		ComponentHierarchyData(List<Component> componentHierarchy, List<?> subcomponentHierarchy) {
			this.componentHierarchy = componentHierarchy;
			this.subcomponentHierarchy = subcomponentHierarchy;
		}

		List<Component> getComponentHierarchy() {
			return componentHierarchy;
		}

		List<?> getSubcomponentHierarchy() {
			return subcomponentHierarchy;
		}
	}

	private static class EvaluationData
	{
		private final String		expression;
		private final ObjectInfo	expressionContext;

		EvaluationData(String expression, ObjectInfo expressionContext) {
			this.expression = expression;
			this.expressionContext = expressionContext;
		}

		String getExpression() {
			return expression;
		}

		ObjectInfo getExpressionContext() {
			return expressionContext;
		}
	}
}
