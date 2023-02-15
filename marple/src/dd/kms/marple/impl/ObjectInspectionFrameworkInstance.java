package dd.kms.marple.impl;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.SecuritySettings;
import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.api.settings.keys.KeySettings;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.security.AccessControlException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectInspectionFrameworkInstance
{
	private InspectionContext	context;

	private Component			lastComponentUnderMouse;
	private Point				lastMousePositionOnComponent;
	private Point				lastMousePositionOnScreen;

	/*
	 * Listener registration/deregistration
	 */
	public void registerSettings(InspectionSettings inspectionSettings) {
		if (context == null) {
			registerAsListener();
		}
		context = new dd.kms.marple.impl.InspectionContextImpl(inspectionSettings);
	}

	public void unregisterSettings() {
		if (context == null) {
			return;
		}
		context = null;
		unregisterAsListener();
	}

	private void registerAsListener() {
		if (canRegisterListeners()) {
			long eventMask = AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;
			Toolkit.getDefaultToolkit().addAWTEventListener(this::eventDispatched, eventMask);
		}
	}

	private void unregisterAsListener() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(this::eventDispatched);
	}

	private static boolean canRegisterListeners() {
		if (GraphicsEnvironment.isHeadless()) {
			return false;
		}
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			try {
				security.checkPermission(new AWTPermission("watchMousePointer"));
			} catch (AccessControlException e) {
				// No allowance to watch mouse coordinates
				return false;
			}
		}
		return true;
	}

	/*
	 * Actions
	 */
	private void performInspection(InspectionContext context, Component component, Point position) {
		Supplier<InspectionAction> actionSupplier = () -> {
			ComponentHierarchy componentHierarchy = dd.kms.marple.impl.gui.ComponentHierarchies.getComponentHierarchy(component, position, context);
			return context.createInspectComponentAction(componentHierarchy);
		};
		performAction(context, actionSupplier);
	}

	private void performEvaluation(InspectionContext context, Component component, Point position) {
		Function<Object, InspectionAction> actionFunction = thisValue -> {
			EvaluationSettings evaluationSettings = context.getSettings().getEvaluationSettings();
			String expression = evaluationSettings.suggestExpressionToEvaluate(thisValue);
			return context.createEvaluateExpressionAction(expression, expression.length(), thisValue);
		};
		performAction(context, component, position, actionFunction);
	}

	private void performSearch(InspectionContext context, Component component, Point position) {
		performAction(context, component, position, context::createSearchInstancesFromHereAction);
	}

	private void openDebugSupportDialog(InspectionContext context, Component component, Point position) {
		performAction(context, component, position, context::createDebugSupportAction);
	}

	private void performCustomAction(InspectionContext context, Component component, Point position, CustomAction customAction) {
		performAction(context, component, position, thisValue -> context.createParameterizedCustomAction(customAction, thisValue));
	}

	private void openCustomActionsDialog(InspectionContext context) {
		dd.kms.marple.impl.gui.common.WindowManager.showInFrame(
			dd.kms.marple.impl.gui.customactions.CustomActionsPanel.TITLE,
			() -> new dd.kms.marple.impl.gui.customactions.CustomActionsPanel(context),
			p -> {},
			p -> {}
		);
	}

	private void openQuickHelp(InspectionContext context) {
		InspectionSettings settings = context.getSettings();
		KeySettings keySettings = settings.getKeySettings();
		CustomActionSettings customActionSettings = settings.getCustomActionSettings();
		dd.kms.marple.impl.gui.common.WindowManager.showInFrame(
			dd.kms.marple.impl.gui.help.QuickHelpPanel.TITLE,
			() -> new dd.kms.marple.impl.gui.help.QuickHelpPanel(keySettings, customActionSettings),
			p -> {},
			p -> {}
		);
	}

	private void performAction(InspectionContext context, Component component, Point position, Function<Object, InspectionAction> actionFunction) {
		Supplier<InspectionAction> actionSupplier = () -> {
			ComponentHierarchy componentHierarchy = dd.kms.marple.impl.gui.ComponentHierarchies.getComponentHierarchy(component, position, context);
			Object componentHierarchyLeaf = componentHierarchy.getSelectedComponent();
			return actionFunction.apply(componentHierarchyLeaf);
		};
		performAction(context, actionSupplier);
	}

	private void performAction(InspectionContext context, Supplier<InspectionAction> actionProvider) {
		if (!userHasPermission(context)) {
			return;
		}
		InspectionAction action = actionProvider.get();
		if (action.isEnabled()) {
			action.perform();
		}
	}

	/*
	 * GUI
	 */
	private boolean userHasPermission(InspectionContext context) {
		InspectionSettings settings = context.getSettings();
		SecuritySettings securitySettings = settings.getSecuritySettings();
		try {
			String passwordHash = securitySettings.hashPassword(securitySettings.queryPassword());
			return Objects.equals(passwordHash, securitySettings.getPasswordHash());
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * Listeners
	 */
	private void eventDispatched(AWTEvent e) {
		if (e instanceof KeyEvent) {
			handleKeyEvent((KeyEvent) e);
		} else if (e instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) e;
			handleMouseOver(mouseEvent.getComponent(), mouseEvent.getPoint());
		}
	}

	private void handleKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			onKeyPressed(new KeyRepresentation(e.getModifiers(), e.getKeyCode()));
		}
	}

	private void handleMouseOver(Component component, Point point) {
		if (component instanceof Container) {
			component = ((Container) component).findComponentAt(point);
		}
		onMouseOverComponentAction(component, point);
	}

	private void onKeyPressed(KeyRepresentation key) {
		if (context == null) {
			return;
		}
		Point mousePosOnScreen = dd.kms.marple.impl.gui.common.GuiCommons.getMousePositionOnScreen();
		if (mousePosOnScreen == null || !mousePosOnScreen.equals(lastMousePositionOnScreen)) {
			return;
		}

		InspectionSettings settings = context.getSettings();
		KeySettings keySettings = settings.getKeySettings();
		KeyRepresentation inspectionKey = keySettings.getInspectionKey();
		KeyRepresentation evaluationKey = keySettings.getEvaluationKey();
		KeyRepresentation findInstancesKey = keySettings.getFindInstancesKey();
		KeyRepresentation debugSupportKey = keySettings.getDebugSupportKey();
		KeyRepresentation customActionsKey = keySettings.getCustomActionsKey();
		KeyRepresentation quickHelpKey = keySettings.getQuickHelpKey();

		if (key.matches(inspectionKey)) {
			performInspection(context, lastComponentUnderMouse, lastMousePositionOnComponent);
		} else if (key.matches(evaluationKey)) {
			performEvaluation(context, lastComponentUnderMouse, lastMousePositionOnComponent);
		} else if (key.matches(findInstancesKey)) {
			performSearch(context, lastComponentUnderMouse, lastMousePositionOnComponent);
		} else if (key.matches(debugSupportKey)) {
			openDebugSupportDialog(context, lastComponentUnderMouse, lastMousePositionOnComponent);
		} else if (key.matches(customActionsKey)) {
			openCustomActionsDialog(context);
		} else if (key.matches(quickHelpKey)) {
			openQuickHelp(context);
		}
		CustomActionSettings customActionSettings = settings.getCustomActionSettings();
		List<CustomAction> customActions = customActionSettings.getCustomActions();
		for (CustomAction customAction : customActions) {
			KeyRepresentation customActionKey = customAction.getKey();
			if (key.matches(customActionKey)) {
				performCustomAction(context, lastComponentUnderMouse, lastMousePositionOnComponent, customAction);
			}
		}
	}

	private void onMouseOverComponentAction(Component component, Point mousePosOnComponent) {
		lastComponentUnderMouse = component;
		lastMousePositionOnComponent = mousePosOnComponent;
		lastMousePositionOnScreen = dd.kms.marple.impl.gui.common.GuiCommons.getMousePositionOnScreen();
	}
}
