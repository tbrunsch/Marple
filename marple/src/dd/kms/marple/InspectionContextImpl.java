package dd.kms.marple;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.actions.AddVariableAction;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.actions.component.SnapshotAction;
import dd.kms.marple.actions.evaluator.EvaluateAsThisAction;
import dd.kms.marple.actions.evaluator.EvaluateExpressionAction;
import dd.kms.marple.actions.history.HistoryActionWrapper;
import dd.kms.marple.actions.history.HistoryBackAction;
import dd.kms.marple.actions.history.HistoryForwardAction;
import dd.kms.marple.actions.inspector.InspectComponentAction;
import dd.kms.marple.actions.inspector.InspectObjectAction;
import dd.kms.marple.actions.search.SearchInstanceAction;
import dd.kms.marple.actions.search.SearchInstancesFromHereAction;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.inspector.InspectionHistory;
import dd.kms.marple.settings.InspectionSettings;
import dd.kms.marple.settings.visual.ObjectView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

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
	public InspectionAction createAddVariableAction(String suggestedName, Object value) {
		return new AddVariableAction(suggestedName, value, this);
	}

	@Override
	public InspectionAction createEvaluateExpressionAction(String expression, Object thisValue) {
		return createEvaluateExpressionAction(expression, thisValue, expression.length());
	}

	@Override
	public InspectionAction createEvaluateExpressionAction(String expression, Object thisValue, int caretPosition) {
		return new EvaluateExpressionAction(settings.getEvaluator(), expression, thisValue, caretPosition);
	}

	@Override
	public InspectionAction createEvaluateAsThisAction(Object thisValue) {
		return new EvaluateAsThisAction(settings.getEvaluator(), thisValue);
	}

	@Override
	public InspectionAction createSearchInstancesFromHereAction(Object root) {
		return new SearchInstancesFromHereAction(this, root);
	}

	@Override
	public InspectionAction createSearchInstanceAction(Object target) {
		return new SearchInstanceAction(this, target);
	}

	@Override
	public <T> InspectionAction createSnapshotAction(T snapshotTarget, Function<T, BufferedImage> snapshotFunction) {
		return new SnapshotAction<>(snapshotTarget, snapshotFunction, this);
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
