package dd.kms.marple.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.component.HighlightComponentAction;
import dd.kms.marple.actions.component.SnapshotAction;
import dd.kms.marple.actions.search.SearchInstanceAction;
import dd.kms.marple.actions.search.SearchInstancesFromHereAction;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.gui.common.Snapshots;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class ActionProviderBuilder
{
	private static final Pattern VARIABLE_NAME_PATTERN	= Pattern.compile("^[A-Za-z][_A-Za-z0-9]*");

	private final String						displayText;
	private final Object						object;
	private final InspectionContext				inspectionContext;

	private @Nullable ComponentHierarchyData	componentHierarchyData;
	private @Nullable String					suggestedVariableName;
	private @Nullable EvaluationData			evaluationData;

	public ActionProviderBuilder(String displayText, List<Component> componentHierarchy, List<?> subcomponentHierarchy, InspectionContext inspectionContext) {
		this.displayText = displayText;
		this.object = ComponentHierarchyModels.getHierarchyLeaf(componentHierarchy, subcomponentHierarchy);
		this.inspectionContext = inspectionContext;

		this.componentHierarchyData = new ComponentHierarchyData(componentHierarchy, subcomponentHierarchy);
	}

	public ActionProviderBuilder(String displayText, Object object, InspectionContext inspectionContext) {
		this.displayText = displayText;
		this.object = object;
		this.inspectionContext = inspectionContext;
	}

	public ActionProviderBuilder suggestVariableName(String suggestedVariableName) {
		if (suggestedVariableName != null && VARIABLE_NAME_PATTERN.matcher(suggestedVariableName).matches()) {
			this.suggestedVariableName = suggestedVariableName;
		}
		return this;
	}

	public ActionProviderBuilder evaluateAs(String expression, Object expressionContext) {
		this.evaluationData = new EvaluationData(expression, expressionContext);
		suggestVariableName(expression);
		return this;
	}

	public ActionProvider build() {
		ImmutableList.Builder<InspectionAction> actionsBuilder = ImmutableList.builder();
		if (object == null) {
			return ActionProvider.of(displayText, actionsBuilder.build());
		}
		boolean isInspectable = ReflectionUtils.isObjectInspectable(object);
		if (componentHierarchyData == null) {
			if (isInspectable) {
				actionsBuilder.add(inspectionContext.createInspectObjectAction(object));
			}
		} else {
			actionsBuilder.add(inspectionContext.createInspectComponentAction(componentHierarchyData.getComponentHierarchy(), componentHierarchyData.getSubcomponentHierarchy()));
		}
		if (object instanceof Component) {
			Component component = (Component) this.object;
			actionsBuilder.add(new HighlightComponentAction(component));
			if (component instanceof JComponent) {
				actionsBuilder.add(inspectionContext.createSnapshotAction((JComponent) component, Snapshots::takeSnapshot));
			}
		}
		if (object instanceof Image) {
			actionsBuilder.add(inspectionContext.createSnapshotAction((Image) object, Snapshots::takeSnapshot));
		}
		if (object instanceof Icon) {
			actionsBuilder.add(inspectionContext.createSnapshotAction((Icon) object, Snapshots::takeSnapshot));
		}
		if (object instanceof Paint) {
			actionsBuilder.add(inspectionContext.createSnapshotAction((Paint) object, paint -> Snapshots.takeSnapshot(paint, 200, 200)));
		}
		actionsBuilder.add(inspectionContext.createAddVariableAction(suggestedVariableName, object));
		actionsBuilder.add(inspectionContext.createEvaluateAsThisAction(object));
		if (evaluationData != null) {
			String expression = evaluationData.getExpression();
			Object expressionContext = evaluationData.getExpressionContext();
			actionsBuilder.add(inspectionContext.createEvaluateExpressionAction(expression, expressionContext));
		}
		actionsBuilder.add(new SearchInstancesFromHereAction(inspectionContext, object));
		actionsBuilder.add(new SearchInstanceAction(inspectionContext, object));
		actionsBuilder.add(new CopyStringRepresentationAction(object));
		return ActionProvider.of(displayText, actionsBuilder.build());
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
		private final String	expression;
		private final Object	expressionContext;

		EvaluationData(String expression, Object expressionContext) {
			this.expression = expression;
			this.expressionContext = expressionContext;
		}

		String getExpression() {
			return expression;
		}

		Object getExpressionContext() {
			return expressionContext;
		}
	}
}
