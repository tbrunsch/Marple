package dd.kms.marple;

import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.gui.common.GuiCommons;
import dd.kms.marple.gui.common.WindowManager;
import dd.kms.marple.gui.help.QuickHelpPanel;
import dd.kms.marple.settings.InspectionSettings;
import dd.kms.marple.settings.SecuritySettings;
import dd.kms.marple.settings.keys.KeyRepresentation;
import dd.kms.marple.settings.keys.KeySettings;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

class ObjectInspectionFrameworkInstance
{
	private InspectionContext	inspectionContext;

	private Component			lastComponentUnderMouse;
	private Point				lastMousePositionOnComponent;
	private Point				lastMousePositionOnScreen;

	/*
	 * Listener registration/deregistration
	 */
	void registerSettings(InspectionSettings inspectionSettings) {
		if (inspectionContext == null) {
			registerAsListener();
		}
		inspectionContext = new InspectionContextImpl(inspectionSettings);
	}

	void unregisterSettings() {
		if (inspectionContext == null) {
			return;
		}
		inspectionContext = null;
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
			List<Component> componentHierarchy = ComponentHierarchyModels.getComponentHierarchy(component);
			List<?> subcomponentHierarchy = context.getSettings().getComponentHierarchyModel().getSubcomponentHierarchy(component, position);
			return context.createInspectComponentAction(componentHierarchy, subcomponentHierarchy);
		};
		performAction(context, actionSupplier);
	}

	private void performEvaluation(InspectionContext context, Component component, Point position) {
		performAction(context, component, position, context::createEvaluateAsThisAction);
	}

	private void performSearch(InspectionContext context, Component component, Point position) {
		performAction(context, component, position, context::createSearchInstancesFromHereAction);
	}

	private void openDebugSupportDialog(InspectionContext context, Component component, Point position) {
		performAction(context, component, position, context::createDebugSupportAction);
	}

	private void openQuickHelp(InspectionContext context) {
		KeySettings keySettings = context.getSettings().getKeySettings();
		WindowManager.showInFrame(QuickHelpPanel.TITLE, () -> new QuickHelpPanel(keySettings), p -> {}, p -> {});
	}

	private void performAction(InspectionContext context, Component component, Point position, Function<ObjectInfo, InspectionAction> actionFunction) {
		Supplier<InspectionAction> actionSupplier = () -> {
			ObjectInfo componentHierarchyLeaf = ComponentHierarchyModels.getHierarchyLeaf(component, position, context);
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
		if (inspectionContext == null) {
			return;
		}
		Point mousePosOnScreen = GuiCommons.getMousePositionOnScreen();
		if (!mousePosOnScreen.equals(lastMousePositionOnScreen)) {
			return;
		}

		InspectionSettings settings = inspectionContext.getSettings();
		KeySettings keySettings = settings.getKeySettings();
		KeyRepresentation inspectionKey = keySettings.getInspectionKey();
		KeyRepresentation evaluationKey = keySettings.getEvaluationKey();
		KeyRepresentation findInstancesKey = keySettings.getFindInstancesKey();
		KeyRepresentation debugSupportKey = keySettings.getDebugSupportKey();
		KeyRepresentation quickHelpKey = keySettings.getQuickHelpKey();

		if (key.matches(inspectionKey)) {
			performInspection(inspectionContext, lastComponentUnderMouse, lastMousePositionOnComponent);
		} else if (key.matches(evaluationKey)) {
			performEvaluation(inspectionContext, lastComponentUnderMouse, lastMousePositionOnComponent);
		} else if (key.matches(findInstancesKey)) {
			performSearch(inspectionContext, lastComponentUnderMouse, lastMousePositionOnComponent);
		} else if (key.matches(debugSupportKey)) {
			openDebugSupportDialog(inspectionContext, lastComponentUnderMouse, lastMousePositionOnComponent);
		} else if (key.matches(quickHelpKey)) {
			openQuickHelp(inspectionContext);
		}
	}

	private void onMouseOverComponentAction(Component component, Point mousePosOnComponent) {
		lastComponentUnderMouse = component;
		lastMousePositionOnComponent = mousePosOnComponent;
		lastMousePositionOnScreen = GuiCommons.getMousePositionOnScreen();
	}
}
