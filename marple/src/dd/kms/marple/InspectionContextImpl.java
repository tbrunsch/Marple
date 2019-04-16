package dd.kms.marple;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.actions.*;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.settings.InspectionSettings;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @param <C>	GUI component class
 * @param <V>	View class (GUI component class plus name)
 * @param <K>	KeyStroke class
 * @param <P>	Point class
 */
class InspectionContextImpl<C, V, K, P> implements InspectionContext<C, V>
{
	private final InspectionSettings<C, V, K, P>	settings;
	private final InspectionHistory					inspectionHistory;

	InspectionContextImpl(InspectionSettings<C, V, K, P> settings) {
		this.settings = settings;
		inspectionHistory = new InspectionHistory();
		settings.getInspector().setInspectionContext(this);
		settings.getEvaluator().setInspectionContext(this);
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
	public InspectionAction createInspectComponentAction(List<C> componentHierarchy, List<?> subcomponentHierarchy) {
		Object hierarchyLeaf = subcomponentHierarchy.isEmpty()
								? componentHierarchy.get(componentHierarchy.size()-1)
								: subcomponentHierarchy.get(subcomponentHierarchy.size()-1);
		String leafDisplayText = getDisplayText(hierarchyLeaf);
		InspectionAction action = new InspectComponentAction<>(settings.getInspector(), componentHierarchy, subcomponentHierarchy, leafDisplayText);
		return new HistoryActionWrapper(inspectionHistory, action);
	}

	@Override
	public InspectionAction createInspectObjectAction(Object object) {
		if (settings.isComponent(object)) {
			C component = (C) object;
			List<C> componentHierarchy = getComponentHierarchy(component);
			return createInspectComponentAction(componentHierarchy, ImmutableList.of());
		}
		InspectionAction action = new InspectObjectAction(settings.getInspector(), object, getDisplayText(object));
		return new HistoryActionWrapper(inspectionHistory, action);
	}

	@Override
	public InspectionAction createInvokeMethodAction(Object object, Method method, Consumer<Object> returnValueConsumer, Consumer<Exception> exceptionConsumer) {
		return new InvokeMethodAction(object, method, returnValueConsumer, exceptionConsumer);
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
	public String getDisplayText(Object object) {
		return settings.getVisualSettings().getDisplayText(object);
	}

	@Override
	public List<V> getInspectionViews(Object object) {
		return settings.getVisualSettings().getInspectionViews(object, this);
	}

	@Override
	public void clearHistory() {
		inspectionHistory.clear();
	}

	InspectionSettings<C, V, K, P> getSettings() {
		return settings;
	}

	void performInspection(C component, P position) {
		List<C> componentHierarchy = getComponentHierarchy(component);
		List<?> subcomponentHierarchy = getSubcomponentHierarchy(component, position);
		InspectionAction inspectComponentAction = createInspectComponentAction(componentHierarchy, subcomponentHierarchy);
		if (inspectComponentAction.isEnabled()) {
			inspectComponentAction.perform();
		}
	}

	void performEvaluation() {
		InspectionAction evaluationAction = createEvaluateExpressionAction(null, null);
		if (evaluationAction.isEnabled()) {
			evaluationAction.perform();
		}
	}

	private List<C> getComponentHierarchy(C component) {
		ImmutableList.Builder<C> componentHierarchyBuilder = ImmutableList.builder();
		ComponentHierarchyModel<C, P> componentHierarchyModel = settings.getComponentHierarchyModel();
		for (C curComponent = component; curComponent != null; curComponent = componentHierarchyModel.getParent(curComponent)) {
			componentHierarchyBuilder.add(curComponent);
		}
		return componentHierarchyBuilder.build().reverse();
	}

	private List<?> getSubcomponentHierarchy(C component, P position) {
		ComponentHierarchyModel<C, P> componentHierarchyModel = settings.getComponentHierarchyModel();
		return ImmutableList.copyOf(componentHierarchyModel.getSubcomponentHierarchy(component, position));
	}
}
