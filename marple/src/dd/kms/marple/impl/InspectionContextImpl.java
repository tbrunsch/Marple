package dd.kms.marple.impl;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.actions.*;
import dd.kms.marple.impl.gui.ComponentHierarchies;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.inspector.InspectionData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public class InspectionContextImpl implements InspectionContext
{
	private final InspectionSettings		settings;
	private final History<InspectionData>	inspectionHistory;

	public InspectionContextImpl(InspectionSettings settings) {
		this.settings = settings;
		inspectionHistory = new History<>();
		settings.getInspector().setInspectionContext(this);
		settings.getEvaluator().setInspectionContext(this);
	}

	@Override
	public InspectionSettings getSettings() {
		return settings;
	}

	@Override
	public InspectionAction createInspectionHistoryBackAction() {
		return new InspectionHistoryBackAction(settings.getInspector(), inspectionHistory);
	}

	@Override
	public InspectionAction createInspectionHistoryForwardAction() {
		return new InspectionHistoryForwardAction(settings.getInspector(), inspectionHistory);
	}

	@Override
	public InspectionAction createInspectComponentAction(ComponentHierarchy componentHierarchy) {
		String componentDisplayText = getDisplayText(componentHierarchy.getSelectedComponent());
		InspectionAction action = new InspectComponentAction(settings.getInspector(), componentHierarchy, componentDisplayText);
		return new HistoryActionWrapper(settings.getInspector(), inspectionHistory, action);
	}

	@Override
	public InspectionAction createInspectObjectAction(Object object) {
		if (object instanceof Component) {
			Component component = (Component) object;
			ComponentHierarchy componentHierarchy = ComponentHierarchies.getComponentHierarchy(component);
			return createInspectComponentAction(componentHierarchy);
		}
		InspectionAction action = new InspectObjectAction(settings.getInspector(), object, getDisplayText(object));
		return new HistoryActionWrapper(settings.getInspector(), inspectionHistory, action);
	}

	@Override
	public InspectionAction createAddVariableAction(String suggestedName, Object value) {
		return new AddVariableAction(suggestedName, value, this);
	}

	@Override
	public InspectionAction createEvaluateExpressionAction(String expression, Object thisValue, int caretPosition) {
		return new EvaluateExpressionAction(settings.getEvaluator(), expression, thisValue, caretPosition);
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
	public InspectionAction createDebugSupportAction(Object thisValue) {
		return new DebugSupportAction(this, thisValue);
	}

	@Override
	public <T> InspectionAction createSnapshotAction(T snapshotTarget, Function<T, BufferedImage> snapshotFunction) {
		return new SnapshotAction<>(snapshotTarget, snapshotFunction, this);
	}

	@Override
	public void clearInspectionHistory() {
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
