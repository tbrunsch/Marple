package dd.kms.marple;

import dd.kms.marple.components.ComponentHierarchyModelBuilder;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.components.SubcomponentHierarchyStrategies;
import dd.kms.marple.settings.InspectionSettings;
import dd.kms.marple.settings.InspectionSettingsBuilder;
import dd.kms.marple.settings.InspectionSettingsBuilders;
import dd.kms.marple.settings.visual.VisualSettingsBuilder;
import dd.kms.marple.settings.visual.VisualSettingsUtils;

public class ObjectInspectionFramework
{
	private static final Object							LOCK							= new Object();
	private static ObjectInspectionFrameworkInstance	FRAMEWORK_INSTANCE				= null;

	/*
	 * Registration/Deregistration
	 */
	public static void register(InspectionSettings inspectionSettings) {
		synchronized (LOCK) {
			ObjectInspectionFrameworkInstance frameworkInstance = getFrameworkInstance(true);
			frameworkInstance.registerSettings(inspectionSettings);
		}
	}

	public static void unregister() {
		synchronized (LOCK) {
			ObjectInspectionFrameworkInstance frameworkInstance = getFrameworkInstance(false);
			if (frameworkInstance != null) {
				frameworkInstance.unregisterSettings();
			}
		}
	}

	/*
	 * Settings
	 */
	public static InspectionSettingsBuilder createInspectionSettingsBuilder() {
		return InspectionSettingsBuilders.create()
			.componentHierarchyModel(createComponentHierarchyModelBuilder().build())
			.visualSettings(createVisualSettingsBuilder().build());
	}

	public static ComponentHierarchyModelBuilder createComponentHierarchyModelBuilder() {
		ComponentHierarchyModelBuilder builder = ComponentHierarchyModels.createBuilder();
		SubcomponentHierarchyStrategies.addDefaultSubcomponentStrategies(builder);
		return builder;
	}

	public static VisualSettingsBuilder createVisualSettingsBuilder() {
		VisualSettingsBuilder builder = VisualSettingsUtils.createBuilder();
		VisualSettingsUtils.addDefaultDisplayTextFunctions(builder);
		VisualSettingsUtils.addDefaultViews(builder);
		return builder;
	}

	private static ObjectInspectionFrameworkInstance getFrameworkInstance(boolean createIfRequired) {
		if (FRAMEWORK_INSTANCE == null && createIfRequired) {
			FRAMEWORK_INSTANCE = new ObjectInspectionFrameworkInstance();
		}
		return FRAMEWORK_INSTANCE;
	}
}
