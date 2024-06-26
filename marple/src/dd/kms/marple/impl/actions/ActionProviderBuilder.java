package dd.kms.marple.impl.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.gui.evaluator.EvaluationFrame;
import dd.kms.marple.impl.gui.inspector.InspectionFrame;
import dd.kms.marple.impl.gui.snapshot.Snapshots;

import javax.annotation.Nullable;
import javax.swing.FocusManager;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ActionProviderBuilder
{
	private static final Pattern VARIABLE_NAME_PATTERN	= Pattern.compile("^[A-Za-z][_A-Za-z0-9]*");

	private final String					displayText;
	private final Object					object;
	private final InspectionContext			context;

	private @Nullable ComponentHierarchy	componentHierarchy;
	private @Nullable String				suggestedVariableName;
	private @Nullable EvaluationData		evaluationData;

	private final List<InspectionAction>	additionalActions		= new ArrayList<>();

	public ActionProviderBuilder(String displayText, ComponentHierarchy componentHierarchy, InspectionContext context) {
		this.displayText = displayText;
		this.object = componentHierarchy.getSelectedComponent();
		this.context = context;

		this.componentHierarchy = componentHierarchy;
	}

	public ActionProviderBuilder(String displayText, Object object, InspectionContext context) {
		this.displayText = displayText;
		this.object = object;
		this.context = context;
	}

	public ActionProviderBuilder suggestVariableName(String suggestedVariableName) {
		if (suggestedVariableName != null && VARIABLE_NAME_PATTERN.matcher(suggestedVariableName).matches()) {
			this.suggestedVariableName = suggestedVariableName;
		}
		return this;
	}

	public ActionProviderBuilder evaluateAs(String expression) {
		return evaluateAs(expression, null);
	}

	public ActionProviderBuilder evaluateAs(String expression, Object expressionContext) {
		this.evaluationData = new EvaluationData(expression, expressionContext);
		suggestVariableName(expression);
		return this;
	}

	public ActionProviderBuilder addAdditionalAction(InspectionAction additionalAction) {
		this.additionalActions.add(additionalAction);
		return this;
	}

	public ActionProvider build() {
		ImmutableList.Builder<InspectionAction> actionsBuilder = ImmutableList.builder();
		if (object == null) {
			return ActionProvider.of(displayText, actionsBuilder.build(), () -> null);
		}
		boolean isInspectable = ReflectionUtils.isObjectInspectable(object);
		if (componentHierarchy == null) {
			if (isInspectable) {
				actionsBuilder.add(context.createInspectObjectAction(object));
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
		actionsBuilder.add(context.createAddVariableAction(suggestedVariableName, this.object));
		actionsBuilder.add(context.createEvaluateExpressionAction("this", "this".length(), this.object));
		if (evaluationData != null) {
			String expression = evaluationData.getExpression();
			Object expressionContext = evaluationData.getExpressionContext();
			actionsBuilder.add(context.createEvaluateExpressionAction(expression, expression.length(), expressionContext));
		}
		actionsBuilder.add(context.createSearchInstancesFromHereAction(object));
		actionsBuilder.add(context.createSearchInstanceAction(object));
		actionsBuilder.add(new CopyStringRepresentationAction(object));
		actionsBuilder.add(context.createDebugSupportAction(object));

		CustomActionSettings customActionSettings = context.getSettings().getCustomActionSettings();
		List<CustomAction> customActions = customActionSettings.getCustomActions();
		for (CustomAction customAction : customActions) {
			Class<?> customActionClass = customAction.getThisClass();
			if (customActionClass.isInstance(object)) {
				actionsBuilder.add(context.createParameterizedCustomAction(customAction, object));
			}
		}

		actionsBuilder.addAll(additionalActions);

		ImmutableList<InspectionAction> actions = actionsBuilder.build();

		return ActionProvider.of(displayText, actions, () -> determineDefaultAction(actions));
	}

	@Nullable
	private InspectionAction determineDefaultAction(List<InspectionAction> actions) {
		Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
		if (activeWindow instanceof InspectionFrame) {
			Predicate<InspectionAction> inspectActionFilter = action ->
				action instanceof InspectObjectAction ||
				action instanceof InspectComponentAction;
			InspectionAction inspectAction = actions.stream().filter(inspectActionFilter).findFirst().orElse(null);
			return inspectAction;
		}
		if (activeWindow instanceof EvaluationFrame) {
			Predicate<InspectionAction> evaluateActionFilter = action ->
				action instanceof EvaluateExpressionAction;
			InspectionAction evaluateAction = actions.stream().filter(evaluateActionFilter).findFirst().orElse(null);
			return evaluateAction;
		}
		return null;
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
