package dd.kms.marple.settings;

import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.settings.visual.VisualSettings;
import dd.kms.marple.settings.visual.VisualSettingsUtils;
import dd.kms.marple.settings.keys.KeySettings;
import dd.kms.marple.settings.keys.KeySettingsBuilders;

import java.awt.*;
import java.util.function.Predicate;

class InspectionSettingsBuilderImpl implements InspectionSettingsBuilder
{
	private ComponentHierarchyModel	componentHierarchyModel	= ComponentHierarchyModels.createBuilder().build();
	private VisualSettings			visualSettings			= VisualSettingsUtils.createBuilder().build();
	private Predicate<Component>	responsibilityPredicate	= component -> true;
	private SecuritySettings 		securitySettings		= NoSecuritySettings.INSTANCE;
	private DebugSettings			debugSettings			= DefaultDebugSettings.INSTANCE;
	private KeySettings				keySettings				= KeySettingsBuilders.create().build();

	@Override
	public InspectionSettingsBuilder componentHierarchyModel(ComponentHierarchyModel componentHierarchyModel) {
		this.componentHierarchyModel = componentHierarchyModel;
		return this;
	}

	@Override
	public InspectionSettingsBuilder visualSettings(VisualSettings visualSettings) {
		this.visualSettings = visualSettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder responsibilityPredicate(Predicate<Component> responsibilityPredicate) {
		this.responsibilityPredicate = responsibilityPredicate;
		return this;
	}

	@Override
	public InspectionSettingsBuilder securitySettings(SecuritySettings securitySettings) {
		this.securitySettings = securitySettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder debugSettings(DebugSettings debugSettings) {
		this.debugSettings = debugSettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder keySettings(KeySettings keySettings) {
		this.keySettings = keySettings;
		return this;
	}

	@Override
	public InspectionSettings build() {
		return new InspectionSettingsImpl(componentHierarchyModel, visualSettings, responsibilityPredicate, securitySettings, debugSettings, keySettings);
	}
}
