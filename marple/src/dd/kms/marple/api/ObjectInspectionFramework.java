package dd.kms.marple.api;

import com.google.common.collect.Multimap;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.InspectionSettingsBuilder;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.api.settings.components.ComponentHierarchyModelBuilder;
import dd.kms.marple.api.settings.components.ComponentHierarchyModels;
import dd.kms.marple.api.settings.visual.VisualSettingsBuilder;
import dd.kms.marple.api.settings.visual.VisualSettingsUtils;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.common.GeneralizedField;

import java.awt.*;

public class ObjectInspectionFramework
{
	private static dd.kms.marple.impl.ObjectInspectionFrameworkInstance FRAMEWORK_INSTANCE = null;

	/**
	 * This method to loads and initializes classes required by Marple whose initializers may require some time.
	 * It is not necessary to call this method, but it can save some time when these classes are required later.
	 */
	public static void preloadClasses() {
		Parsers.preloadClasses();
	}

	/*
	 * Registration/Deregistration
	 */
	public static synchronized void register(InspectionSettings inspectionSettings) {
		dd.kms.marple.impl.ObjectInspectionFrameworkInstance frameworkInstance = getFrameworkInstance(true);
		frameworkInstance.registerSettings(inspectionSettings);
	}

	public static synchronized void unregister() {
		dd.kms.marple.impl.ObjectInspectionFrameworkInstance frameworkInstance = getFrameworkInstance(false);
		if (frameworkInstance != null) {
			frameworkInstance.unregisterSettings();
		}
	}

	/*
	 * Settings
	 */
	public static InspectionSettingsBuilder createInspectionSettingsBuilder() {
		return InspectionSettingsBuilder.create()
			.componentHierarchyModel(createComponentHierarchyModelBuilder().build())
			.visualSettings(createVisualSettingsBuilder().build());
	}

	public static ComponentHierarchyModelBuilder createComponentHierarchyModelBuilder() {
		ComponentHierarchyModelBuilder builder = ComponentHierarchyModelBuilder.create();
		ComponentHierarchyModels.addDefaultSubcomponentStrategies(builder);
		return builder;
	}

	public static VisualSettingsBuilder createVisualSettingsBuilder() {
		VisualSettingsBuilder builder = VisualSettingsBuilder.create();
		VisualSettingsUtils.addDefaultDisplayTextFunctions(builder);
		VisualSettingsUtils.addDefaultViews(builder);
		return builder;
	}

	/*
	 * Component Hierarchies
	 */

	/**
	 * Returns the hierarchy from the root component (usually a window) to the specified component.
	 */
	public static ComponentHierarchy getComponentHierarchy(Component component) {
		return dd.kms.marple.impl.gui.ComponentHierarchies.getComponentHierarchy(component);
	}

	/**
	 * Returns for each object of the specified component hierarchy the fields of objects of the
	 * hierarchy that reference this object. This information helps finding the code that creates
	 * or holds a certain component.
	 */
	public static Multimap<Object, GeneralizedField> findReferencingFields(ComponentHierarchy componentHierarchy) {
		return dd.kms.marple.impl.gui.ComponentHierarchies.findReferencingFields(componentHierarchy);
	}

	private static dd.kms.marple.impl.ObjectInspectionFrameworkInstance getFrameworkInstance(boolean createIfRequired) {
		if (FRAMEWORK_INSTANCE == null && createIfRequired) {
			FRAMEWORK_INSTANCE = new dd.kms.marple.impl.ObjectInspectionFrameworkInstance();
		}
		return FRAMEWORK_INSTANCE;
	}
}
