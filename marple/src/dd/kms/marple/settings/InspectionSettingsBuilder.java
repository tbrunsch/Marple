package dd.kms.marple.settings;

import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.settings.keys.KeySettings;
import dd.kms.marple.settings.visual.VisualSettings;

public interface InspectionSettingsBuilder
{
	InspectionSettingsBuilder componentHierarchyModel(ComponentHierarchyModel componentHierarchyModel);
	InspectionSettingsBuilder visualSettings(VisualSettings visualSettings);
	InspectionSettingsBuilder securitySettings(SecuritySettings securitySettings);
	InspectionSettingsBuilder debugSettings(DebugSettings debugSettings);
	InspectionSettingsBuilder keySettings(KeySettings keySettings);
	InspectionSettings build();
}
