package dd.kms.marple.api.settings;

import dd.kms.marple.api.settings.components.ComponentHierarchyModel;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.api.settings.visual.VisualSettings;

public interface InspectionSettingsBuilder
{
	static InspectionSettingsBuilder create() {
		return new dd.kms.marple.impl.settings.InspectionSettingsBuilderImpl();
	}

	InspectionSettingsBuilder componentHierarchyModel(ComponentHierarchyModel componentHierarchyModel);
	InspectionSettingsBuilder visualSettings(VisualSettings visualSettings);
	InspectionSettingsBuilder securitySettings(SecuritySettings securitySettings);
	InspectionSettingsBuilder debugSettings(DebugSettings debugSettings);
	InspectionSettingsBuilder keySettings(KeySettings keySettings);
	InspectionSettings build();
}
