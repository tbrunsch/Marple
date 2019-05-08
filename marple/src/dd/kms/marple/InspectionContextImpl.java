package dd.kms.marple;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.actions.*;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.gui.ObjectView;
import dd.kms.marple.inspector.InspectionHistory;
import dd.kms.marple.settings.InspectionSettings;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

class InspectionContextImpl implements InspectionContext
{
	private final InspectionSettings	settings;
	private final InspectionHistory 	inspectionHistory;

	InspectionContextImpl(InspectionSettings settings) {
		this.settings = settings;
		inspectionHistory = new InspectionHistory();
		settings.getInspector().setInspectionContext(this);
		settings.getEvaluator().setInspectionContext(this);
	}

	@Override
	public InspectionSettings getSettings() {
		return settings;
	}

	@Override
	public InspectionAction createHistoryBackAction() {
		return new HistoryBackAction(inspectionHistory);
	}

	@Override
	public InspectionAction createHistoryForwardAction() {
		return new HistoryForwardAction(inspectionHistory);
	}

	@Override
	public InspectionAction createInspectComponentAction(List<Component> componentHierarchy, List<?> subcomponentHierarchy) {
		Object hierarchyLeaf = ComponentHierarchyModels.getHierarchyLeaf(componentHierarchy, subcomponentHierarchy);
		String leafDisplayText = getDisplayText(hierarchyLeaf);
		InspectionAction action = new InspectComponentAction(settings.getInspector(), componentHierarchy, subcomponentHierarchy, leafDisplayText);
		return new HistoryActionWrapper(inspectionHistory, action);
	}

	@Override
	public InspectionAction createInspectObjectAction(Object object) {
		if (object instanceof Component) {
			Component component = (Component) object;
			List<Component> componentHierarchy = ComponentHierarchyModels.getComponentHierarchy(component);
			return createInspectComponentAction(componentHierarchy, ImmutableList.of());
		}
		InspectionAction action = new InspectObjectAction(settings.getInspector(), object, getDisplayText(object));
		return new HistoryActionWrapper(inspectionHistory, action);
	}

	@Override
	public InspectionAction createHighlightComponentAction(Component component) {
		return new HighlightComponentAction(settings.getInspector(), component, getDisplayText(component));
	}

	@Override
	public InspectionAction createInvokeMethodAction(Object object, Method method, Consumer<Object> returnValueConsumer, Consumer<Exception> exceptionConsumer) {
		return new InvokeMethodAction(object, method, returnValueConsumer, exceptionConsumer);
	}

	@Override
	public InspectionAction createAddVariableAction(String suggestedName, Object value) {
		return new AddVariableAction(suggestedName, value, this);
	}

	@Override
	public InspectionAction createEvaluateExpressionAction(String expression, Object thisValue) {
		return new EvaluateExpressionAction(settings.getEvaluator(), expression, thisValue);
	}

	@Override
	public InspectionAction createEvaluateAsThisAction(Object thisValue) {
		return new EvaluateAsThisAction(settings.getEvaluator(), thisValue);
	}

	@Override
	public void clearHistory() {
		inspectionHistory.clear();
	}

	@Override
	public String getDisplayText(Object object) {
		return settings.getVisualSettings().getDisplayText(object);
	}

	@Override
	public List<ObjectView> getInspectionViews(Object object) {
		return settings.getVisualSettings().getInspectionViews(object, this);
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return settings.getEvaluator();
	}
}
