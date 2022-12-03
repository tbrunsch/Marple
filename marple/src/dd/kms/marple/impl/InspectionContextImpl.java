package dd.kms.marple.impl;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.actions.*;
import dd.kms.marple.impl.gui.ComponentHierarchies;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public class InspectionContextImpl implements InspectionContext
{
	private final InspectionSettings		settings;

	public InspectionContextImpl(InspectionSettings settings) {
		this.settings = settings;
		settings.getInspector().setInspectionContext(this);
		settings.getEvaluator().setInspectionContext(this);
	}

	@Override
	public InspectionSettings getSettings() {
		return settings;
	}

	@Override
	public InspectionAction createInspectComponentAction(ComponentHierarchy componentHierarchy) {
		String componentDisplayText = getDisplayText(componentHierarchy.getSelectedComponent());
		return new InspectComponentAction(settings.getInspector(), componentHierarchy, componentDisplayText);
	}

	@Override
	public InspectionAction createInspectObjectAction(Object object) {
		if (object instanceof Component) {
			Component component = (Component) object;
			ComponentHierarchy componentHierarchy = ComponentHierarchies.getComponentHierarchy(component);
			return createInspectComponentAction(componentHierarchy);
		}
		return new InspectObjectAction(settings.getInspector(), object, getDisplayText(object));
	}

	@Override
	public InspectionAction createAddVariableAction(String suggestedName, Object value) {
		return new AddVariableAction(suggestedName, value, this);
	}

	@Override
	public InspectionAction createEvaluateExpressionAction(String expression, int caretPosition, Object thisValue) {
		return new EvaluateExpressionAction(settings.getEvaluator(), expression, caretPosition, thisValue);
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
