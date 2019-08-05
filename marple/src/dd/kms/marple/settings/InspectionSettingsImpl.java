package dd.kms.marple.settings;

import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.marple.settings.visual.VisualSettings;
import dd.kms.marple.inspector.ObjectInspector;
import dd.kms.marple.inspector.ObjectInspectors;
import dd.kms.marple.settings.keys.KeySettings;

import java.awt.*;
import java.util.function.Predicate;

class InspectionSettingsImpl implements InspectionSettings
{
	private final ObjectInspector			inspector;
	private final ExpressionEvaluator		evaluator;
	private final ComponentHierarchyModel	componentHierarchyModel;
	private final VisualSettings			visualSettings;
	private final Predicate<Component>		responsibilityPredicate;
	private final SecuritySettings			securitySettings;
	private final KeySettings				keySettings;

	InspectionSettingsImpl(ComponentHierarchyModel componentHierarchyModel, VisualSettings visualSettings, Predicate<Component> responsibilityPredicate,
			SecuritySettings securitySettings, KeySettings keySettings) {
		this.inspector = ObjectInspectors.create();
		this.evaluator = ExpressionEvaluators.create();
		this.componentHierarchyModel = componentHierarchyModel;
		this.visualSettings = visualSettings;
		this.responsibilityPredicate = responsibilityPredicate;
		this.securitySettings = securitySettings;
		this.keySettings = keySettings;
	}

	@Override
	public ObjectInspector getInspector() {
		return inspector;
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return evaluator;
	}

	@Override
	public ComponentHierarchyModel getComponentHierarchyModel() {
		return componentHierarchyModel;
	}

	@Override
	public VisualSettings getVisualSettings() {
		return visualSettings;
	}

	@Override
	public Predicate<Component> getResponsibilityPredicate() {
		return responsibilityPredicate;
	}

	@Override
	public SecuritySettings getSecuritySettings() {
		return securitySettings;
	}

	@Override
	public KeySettings getKeySettings() {
		return keySettings;
	}
}
