package dd.kms.marple.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.components.ComponentHierarchyModels;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class ActionProviderBuilder
{
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
		this.suggestedVariableName = suggestedVariableName;
		return this;
	}

	public ActionProviderBuilder evaluateAs(String expression, Object expressionContext) {
		this.evaluationData = new EvaluationData(expression, expressionContext);
		suggestVariableName(expression);
		return this;
	}

	public ActionProvider build() {
		ImmutableList.Builder<InspectionAction> actionsBuilder = ImmutableList.builder();
		if (object != null) {
			Class<?> objectClass = object.getClass();
			boolean isPrimitive = objectClass.isPrimitive() || Primitives.isWrapperType(objectClass);
			if (componentHierarchyData == null) {
				if (!isPrimitive) {
					actionsBuilder.add(inspectionContext.createInspectObjectAction(object));
				}
			} else {
				actionsBuilder.add(inspectionContext.createInspectComponentAction(componentHierarchyData.getComponentHierarchy(), componentHierarchyData.getSubcomponentHierarchy()));
			}
			if (object instanceof Component) {
				actionsBuilder.add(inspectionContext.createHighlightComponentAction((Component) object));
			}
			actionsBuilder.add(inspectionContext.createAddVariableAction(suggestedVariableName, object));
			actionsBuilder.add(inspectionContext.createEvaluateAsThisAction(object));
			if (evaluationData != null) {
				String expression = evaluationData.getExpression();
				Object expressionContext = evaluationData.getExpressionContext();
				actionsBuilder.add(inspectionContext.createEvaluateExpressionAction(expression, expressionContext));
			}
		}
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
