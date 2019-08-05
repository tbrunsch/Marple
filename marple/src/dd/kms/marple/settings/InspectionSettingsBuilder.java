package dd.kms.marple.settings;

import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.settings.visual.VisualSettings;
import dd.kms.marple.settings.keys.KeySettings;

import java.awt.*;
import java.util.function.Predicate;

public interface InspectionSettingsBuilder
{
	InspectionSettingsBuilder componentHierarchyModel(ComponentHierarchyModel componentHierarchyModel);
	InspectionSettingsBuilder visualSettings(VisualSettings visualSettings);
	InspectionSettingsBuilder responsibilityPredicate(Predicate<Component> responsibilityPredicate);
	InspectionSettingsBuilder securitySettings(SecuritySettings securitySettings);
	InspectionSettingsBuilder keySettings(KeySettings keySettings);
	InspectionSettings build();
}
