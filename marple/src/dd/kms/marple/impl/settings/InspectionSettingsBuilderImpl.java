package dd.kms.marple.impl.settings;

import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.InspectionSettingsBuilder;
import dd.kms.marple.api.settings.SecuritySettings;
import dd.kms.marple.api.settings.components.ComponentHierarchyModel;
import dd.kms.marple.api.settings.components.ComponentHierarchyModelBuilder;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.api.settings.keys.KeySettingsBuilder;
import dd.kms.marple.api.settings.visual.VisualSettings;
import dd.kms.marple.api.settings.visual.VisualSettingsBuilder;

public class InspectionSettingsBuilderImpl implements InspectionSettingsBuilder
{
	private ComponentHierarchyModel	componentHierarchyModel		= ComponentHierarchyModelBuilder.create().build();
	private EvaluationSettings		evaluationSettings			= EvaluationSettingsBuilder.create().build();
	private VisualSettings			visualSettings				= VisualSettingsBuilder.create().build();
	private SecuritySettings		securitySettings			= NoSecuritySettings.INSTANCE;
	private KeySettings				keySettings					= KeySettingsBuilder.create().build();
	private String					triggerBreakpointExpression	= "dd.kms.marple.api.DebugSupport.triggerBreakpoint(this)";

	@Override
	public InspectionSettingsBuilder componentHierarchyModel(ComponentHierarchyModel componentHierarchyModel) {
		this.componentHierarchyModel = componentHierarchyModel;
		return this;
	}

	@Override
	public InspectionSettingsBuilder evaluationSettings(EvaluationSettings evaluationSettings) {
		this.evaluationSettings = evaluationSettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder visualSettings(VisualSettings visualSettings) {
		this.visualSettings = visualSettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder securitySettings(SecuritySettings securitySettings) {
		this.securitySettings = securitySettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder keySettings(KeySettings keySettings) {
		this.keySettings = keySettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder triggerBreakpointExpression(String triggerBreakpointExpression) {
		this.triggerBreakpointExpression = triggerBreakpointExpression;
		return this;
	}

	@Override
	public InspectionSettings build() {
		return new InspectionSettingsImpl(componentHierarchyModel, evaluationSettings, visualSettings, securitySettings, keySettings, triggerBreakpointExpression);
	}
}
