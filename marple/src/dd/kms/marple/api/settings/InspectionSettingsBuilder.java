package dd.kms.marple.api.settings;

import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchyModel;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.api.settings.visual.VisualSettings;

import java.nio.file.Path;

public interface InspectionSettingsBuilder
{
	static InspectionSettingsBuilder create() {
		return new dd.kms.marple.impl.settings.InspectionSettingsBuilderImpl();
	}

	InspectionSettingsBuilder componentHierarchyModel(ComponentHierarchyModel componentHierarchyModel);
	InspectionSettingsBuilder evaluationSettings(EvaluationSettings evaluationSettings);
	InspectionSettingsBuilder visualSettings(VisualSettings visualSettings);
	InspectionSettingsBuilder customActionSettings(CustomActionSettings customActionSettings);
	InspectionSettingsBuilder securitySettings(SecuritySettings securitySettings);
	InspectionSettingsBuilder keySettings(KeySettings keySettings);

	/**
	 * Specify a file according to which Marple decides where to store its preferences.
	 * This will be similar to the specified file, but deviate from it since different
	 * Marple versions that use different preferences file formats must write into
	 * different files.
	 */
	InspectionSettingsBuilder preferencesFile(Path preferencesFile);
	InspectionSettings build();
}
