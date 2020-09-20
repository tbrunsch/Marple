package dd.kms.marple.impl;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.actions.AddVariableAction;
import dd.kms.marple.impl.actions.SnapshotAction;
import dd.kms.marple.impl.actions.DebugSupportAction;
import dd.kms.marple.impl.actions.EvaluateAsThisAction;
import dd.kms.marple.impl.actions.EvaluateExpressionAction;
import dd.kms.marple.impl.actions.HistoryActionWrapper;
import dd.kms.marple.impl.actions.HistoryBackAction;
import dd.kms.marple.impl.actions.HistoryForwardAction;
import dd.kms.marple.impl.actions.InspectComponentAction;
import dd.kms.marple.impl.actions.InspectObjectAction;
import dd.kms.marple.impl.actions.SearchInstanceAction;
import dd.kms.marple.impl.actions.SearchInstancesFromHereAction;
import dd.kms.marple.impl.gui.ComponentHierarchies;
import dd.kms.marple.impl.inspector.InspectionHistory;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public class InspectionContextImpl implements InspectionContext
{
	private final InspectionSettings	settings;
	private final InspectionHistory 	inspectionHistory;

	public InspectionContextImpl(InspectionSettings settings) {
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
	public InspectionAction createInspectComponentAction(ComponentHierarchy componentHierarchy) {
		String componentDisplayText = getDisplayText(InfoProvider.createObjectInfo(componentHierarchy.getSelectedComponent()));
		InspectionAction action = new InspectComponentAction(settings.getInspector(), componentHierarchy, componentDisplayText);
		return new HistoryActionWrapper(inspectionHistory, action);
	}

	@Override
	public InspectionAction createInspectObjectAction(ObjectInfo objectInfo) {
		Object object = objectInfo.getObject();
		if (object instanceof Component) {
			Component component = (Component) object;
			ComponentHierarchy componentHierarchy = ComponentHierarchies.getComponentHierarchy(component);
			return createInspectComponentAction(componentHierarchy);
		}
		InspectionAction action = new InspectObjectAction(settings.getInspector(), objectInfo, getDisplayText(objectInfo));
		return new HistoryActionWrapper(inspectionHistory, action);
	}

	@Override
	public InspectionAction createAddVariableAction(String suggestedName, ObjectInfo valueInfo) {
		return new AddVariableAction(suggestedName, valueInfo, this);
	}

	@Override
	public InspectionAction createEvaluateExpressionAction(String expression, ObjectInfo thisValue) {
		return createEvaluateExpressionAction(expression, thisValue, expression.length());
	}

	@Override
	public InspectionAction createEvaluateExpressionAction(String expression, ObjectInfo thisValue, int caretPosition) {
		return new EvaluateExpressionAction(settings.getEvaluator(), expression, thisValue, caretPosition);
	}

	@Override
	public InspectionAction createEvaluateAsThisAction(ObjectInfo thisValue) {
		return new EvaluateAsThisAction(settings.getEvaluator(), thisValue);
	}

	@Override
	public InspectionAction createSearchInstancesFromHereAction(ObjectInfo root) {
		return new SearchInstancesFromHereAction(this, root);
	}

	@Override
	public InspectionAction createSearchInstanceAction(ObjectInfo target) {
		return new SearchInstanceAction(this, target);
	}

	@Override
	public InspectionAction createDebugSupportAction(ObjectInfo thisValue) {
		return new DebugSupportAction(this, thisValue);
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
	public String getDisplayText(ObjectInfo objectInfo) {
		return settings.getVisualSettings().getDisplayText(objectInfo);
	}

	@Override
	public List<ObjectView> getInspectionViews(ObjectInfo objectInfo) {
		return settings.getVisualSettings().getInspectionViews(objectInfo, this);
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return settings.getEvaluator();
	}
}
