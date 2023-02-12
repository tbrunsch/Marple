package dd.kms.marple.impl.settings;

import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.SecuritySettings;
import dd.kms.marple.api.settings.components.ComponentHierarchyModel;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.api.settings.visual.VisualSettings;

class InspectionSettingsImpl implements InspectionSettings
{
	private final ObjectInspector			inspector;
	private final ExpressionEvaluator		evaluator;
	private final ComponentHierarchyModel	componentHierarchyModel;
	private final EvaluationSettings		evaluationSettings;
	private final VisualSettings			visualSettings;
	private final SecuritySettings			securitySettings;
	private final KeySettings				keySettings;

	private String							triggerBreakpointExpression;

	InspectionSettingsImpl(ComponentHierarchyModel componentHierarchyModel, EvaluationSettings evaluationSettings, VisualSettings visualSettings,
						   SecuritySettings securitySettings, KeySettings keySettings, String triggerBreakpointExpression) {
		this.evaluationSettings = evaluationSettings;
		this.inspector = ObjectInspector.create();
		this.evaluator = ExpressionEvaluator.create();
		this.componentHierarchyModel = componentHierarchyModel;
		this.visualSettings = visualSettings;
		this.securitySettings = securitySettings;
		this.keySettings = keySettings;
		this.triggerBreakpointExpression = triggerBreakpointExpression;
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
	public EvaluationSettings getEvaluationSettings() {
		return evaluationSettings;
	}

	@Override
	public VisualSettings getVisualSettings() {
		return visualSettings;
	}

	@Override
	public SecuritySettings getSecuritySettings() {
		return securitySettings;
	}

	@Override
	public KeySettings getKeySettings() {
		return keySettings;
	}

	@Override
	public String getTriggerBreakpointExpression() {
		return triggerBreakpointExpression;
	}

	@Override
	public void setTriggerBreakpointExpression(String triggerBreakpointExpression) {
		this.triggerBreakpointExpression = triggerBreakpointExpression;
	}
}
