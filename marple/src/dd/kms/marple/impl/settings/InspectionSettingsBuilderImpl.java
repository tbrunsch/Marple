package dd.kms.marple.impl.settings;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.InspectionSettingsBuilder;
import dd.kms.marple.api.settings.SecuritySettings;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchyModel;
import dd.kms.marple.api.settings.components.ComponentHierarchyModelBuilder;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.api.settings.keys.KeySettingsBuilder;
import dd.kms.marple.api.settings.visual.VisualSettings;
import dd.kms.marple.api.settings.visual.VisualSettingsBuilder;

import javax.annotation.Nullable;
import java.nio.file.Path;

public class InspectionSettingsBuilderImpl implements InspectionSettingsBuilder
{
	private ComponentHierarchyModel	componentHierarchyModel		= ComponentHierarchyModelBuilder.create().build();
	private EvaluationSettings		evaluationSettings			= EvaluationSettingsBuilder.create().build();
	private VisualSettings			visualSettings				= VisualSettingsBuilder.create().build();
	private CustomActionSettings	customActionSettings		= CustomActionSettings.of(ImmutableList.of());
	private SecuritySettings		securitySettings			= NoSecuritySettings.INSTANCE;
	private KeySettings				keySettings					= KeySettingsBuilder.create().build();
	@Nullable
	private Path					preferencesFile;

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
	public InspectionSettingsBuilder customActionSettings(CustomActionSettings customActionSettings) {
		this.customActionSettings = CustomActionSettings.of(customActionSettings.getCustomActions());
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
	public InspectionSettingsBuilder preferencesFile(Path preferencesFile) {
		this.preferencesFile = preferencesFile;
		return this;
	}

	@Override
	public InspectionSettings build() {
		return new InspectionSettingsImpl(componentHierarchyModel, evaluationSettings, visualSettings, customActionSettings, securitySettings, keySettings, preferencesFile);
	}
}
