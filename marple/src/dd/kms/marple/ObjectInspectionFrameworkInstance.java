package dd.kms.marple;

import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.settings.InspectionSettings;
import dd.kms.marple.settings.SecuritySettings;
import dd.kms.marple.settings.keys.KeyRepresentation;
import dd.kms.marple.settings.keys.KeySettings;

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
	private final Map<Object, InspectionContext>	managedInspectionContexts	= new HashMap<>();

	private Component								lastComponentUnderMouse;
	private Point									lastMousePositionOnComponent;
	private Point									lastMousePositionOnScreen;

	/*
	 * Listener registration/deregistration
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

	private void performAction(InspectionContext context, Component component, Point position, Function<Object, InspectionAction> actionFunction) {
		Supplier<InspectionAction> actionSupplier = () -> {
			Object componentHierarchyLeaf = ComponentHierarchyModels.getHierarchyLeaf(component, position, context);
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
	private Point getMousePositionOnScreen() {
		return MouseInfo.getPointerInfo().getLocation();
	}

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
			KeySettings keySettings = settings.getKeySettings();
			KeyRepresentation inspectionKey = keySettings.getInspectionKey();
			KeyRepresentation evaluationKey = keySettings.getEvaluationKey();
			KeyRepresentation searchKey = keySettings.getSearchKey();

			if (key.matches(inspectionKey)) {
				performInspection(context, lastComponentUnderMouse, lastMousePositionOnComponent);
			} else if (key.matches(evaluationKey)) {
				performEvaluation(context, lastComponentUnderMouse, lastMousePositionOnComponent);
			} else if (key.matches(searchKey)) {
				performSearch(context, lastComponentUnderMouse, lastMousePositionOnComponent);
			}
		}
	}

	private void onMouseOverComponentAction(Component component, Point mousePosOnComponent) {
		lastComponentUnderMouse = component;
		lastMousePositionOnComponent = mousePosOnComponent;
		lastMousePositionOnScreen = getMousePositionOnScreen();
	}
}
