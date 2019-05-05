package dd.kms.marple;

import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.settings.InspectionSettings;
import dd.kms.marple.settings.KeyRepresentation;
import dd.kms.marple.settings.SecuritySettings;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class ObjectInspectionFrameworkInstance
{
	private final Map<Object, InspectionContext>	managedInspectionContexts	= new HashMap<>();

	private Component								lastComponentUnderMouse;
	private Point									lastMousePositionOnComponent;
	private Point									lastMousePositionOnScreen;

	/*
	 * Listener registration/unregistration
	 */
	void registerSettings(Object identifier, InspectionSettings inspectionSettings) {
		if (managedInspectionContexts.isEmpty()) {
			registerAsListener();
		}
		managedInspectionContexts.put(identifier, new InspectionContextImpl(inspectionSettings));
	}

	void unregisterSettings(Object identifier) {
		if (managedInspectionContexts.isEmpty()) {
			return;
		}
		managedInspectionContexts.remove(identifier);
		if (managedInspectionContexts.isEmpty()) {
			unregisterAsListener();
		}
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
		List<Component> componentHierarchy = ComponentHierarchyModels.getComponentHierarchy(component);
		List<?> subcomponentHierarchy = context.getSettings().getComponentHierarchyModel().getSubcomponentHierarchy(component, position);
		InspectionAction inspectComponentAction = context.createInspectComponentAction(componentHierarchy, subcomponentHierarchy);
		if (inspectComponentAction.isEnabled()) {
			inspectComponentAction.perform();
		}
	}

	private void performEvaluation(InspectionContext context) {
		InspectionAction evaluationAction = context.createEvaluateExpressionAction(null, null);
		if (evaluationAction.isEnabled()) {
			evaluationAction.perform();
		}
	}

	/*
	 * GUI
	 */
	private Point getMousePositionOnScreen() {
		return MouseInfo.getPointerInfo().getLocation();
	}

	private boolean userHasPermission(InspectionSettings settings) {
		Optional<SecuritySettings> securitySettingsOptional = settings.getSecuritySettings();
		if (!securitySettingsOptional.isPresent()) {
			return true;
		}
		SecuritySettings securitySettings = securitySettingsOptional.get();
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
			onKeyPressed(new KeyRepresentation(e.getKeyCode(), e.getModifiers()));
		}
	}

	private void handleMouseOver(Component component, Point point) {
		if (component instanceof Container) {
			component = ((Container) component).findComponentAt(point);
		}
		onMouseOverComponentAction(component, point);
	}

	private void onKeyPressed(KeyRepresentation key) {
		Point mousePosOnScreen = getMousePositionOnScreen();
		if (!mousePosOnScreen.equals(lastMousePositionOnScreen)) {
			return;
		}

		for (InspectionContext context : managedInspectionContexts.values()) {
			InspectionSettings settings = context.getSettings();
			if (lastComponentUnderMouse != null && !settings.getResponsibilityPredicate().test(lastComponentUnderMouse)) {
				continue;
			}
			KeyRepresentation inspectionKey = settings.getInspectionKey();
			KeyRepresentation evaluationKey = settings.getEvaluationKey();

			if (key.matches(inspectionKey)) {
				if (userHasPermission(settings)) {
					performInspection(context, lastComponentUnderMouse, lastMousePositionOnComponent);
					return;
				}
			} else if (key.matches(evaluationKey)) {
				if (userHasPermission(settings)) {
					performEvaluation(context);
					return;
				}
			}
		}
	}

	private void onMouseOverComponentAction(Component component, Point mousePosOnComponent) {
		lastComponentUnderMouse = component;
		lastMousePositionOnComponent = mousePosOnComponent;
		lastMousePositionOnScreen = getMousePositionOnScreen();
	}
}
