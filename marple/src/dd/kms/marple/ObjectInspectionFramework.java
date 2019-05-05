package dd.kms.marple;

import dd.kms.marple.components.ComponentHierarchyModelBuilder;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.components.SubcomponentHierarchyStrategies;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.marple.gui.VisualSettingsBuilder;
import dd.kms.marple.gui.VisualSettingsUtils;
import dd.kms.marple.inspector.ObjectInspectors;
import dd.kms.marple.settings.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ObjectInspectionFramework
{
	private static final Object							LOCK							= new Object();
	private static ObjectInspectionFrameworkInstance	FRAMEWORK_INSTANCE				= null;

	private static final KeyRepresentation				INSPECTION_KEY					= new KeyRepresentation(KeyEvent.VK_I, 		KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
	private static final KeyRepresentation				EVALUATION_KEY					= new KeyRepresentation(KeyEvent.VK_F8,		KeyEvent.ALT_MASK);
	private static final KeyRepresentation				CODE_COMPLETION_KEY 			= new KeyRepresentation(KeyEvent.VK_SPACE,	KeyEvent.CTRL_MASK);
	private static final KeyRepresentation				SHOW_EXECUTABLE_ARGUMENTS_KEY	= new KeyRepresentation(KeyEvent.VK_P,		KeyEvent.CTRL_MASK);

	/*
	 * Registration/Unregistration
	 */
	public static void register(Object identifier, InspectionSettings inspectionSettings) {
		synchronized (LOCK) {
			ObjectInspectionFrameworkInstance frameworkInstance = getFrameworkInstance(true);
			frameworkInstance.registerSettings(identifier, inspectionSettings);
		}
	}

	public static void unregister(Object identifier) {
		synchronized (LOCK) {
			ObjectInspectionFrameworkInstance frameworkInstance = getFrameworkInstance(false);
			if (frameworkInstance != null) {
				frameworkInstance.unregisterSettings(identifier);
			}
		}
	}

	/*
	 * Settings
	 */
	public static InspectionSettingsBuilder createInspectionSettingsBuilder() {
		return InspectionSettingsBuilders.create()
			.inspector(ObjectInspectors.create())
			.evaluator(ExpressionEvaluators.create())
			.componentHierarchyModel(createComponentHierarchyModelBuilder().build())
			.visualSettings(createVisualSettingsBuilder().build())
			.inspectionKey(INSPECTION_KEY)
			.evaluationKey(EVALUATION_KEY)
			.codeCompletionKey(CODE_COMPLETION_KEY)
			.showMethodArgumentsKey(SHOW_EXECUTABLE_ARGUMENTS_KEY);
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
