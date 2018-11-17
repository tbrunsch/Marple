package com.AMS.jBEAM.objectInspection.swing;

import sun.security.util.SecurityConstants;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.security.AccessControlException;

class SwingEventHandler
{
	static void register(SwingObjectInspector objectInspector, SwingKeyboardShortCut keyboardShortCut) {
		SwingEventHandler eventHandler = new SwingEventHandler(objectInspector, keyboardShortCut);
		eventHandler.register();
	}

	private final SwingObjectInspector	objectInspector;
	private final SwingKeyboardShortCut	keyboardShortCut;

	private Component					lastObservedComponent;
	private Point						lastObservedMouseLocationOnComponent;
	private Point						lastObservedMouseLocationOnScreen;

	private SwingEventHandler(SwingObjectInspector objectInspector, SwingKeyboardShortCut keyboardShortCut) {
		this.objectInspector = objectInspector;
		this.keyboardShortCut = keyboardShortCut;
	}

	private void register() {
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			try {
				security.checkPermission(SecurityConstants.AWT.WATCH_MOUSE_PERMISSION);
			} catch (AccessControlException e) {
				// No allowance to watch mouse coordinates
				return;
			}
		}
		Toolkit tk = Toolkit.getDefaultToolkit();
		long eventMask = AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;
		tk.addAWTEventListener(this::eventDispatched, eventMask);
	}

	private void eventDispatched(AWTEvent e) {
		if (e instanceof KeyEvent) {
			onKeyEventDispatched((KeyEvent) e);
		} else if (e instanceof MouseEvent) {
			onMouseEventDispatched((MouseEvent) e);
		}
	}

	private void onKeyEventDispatched(KeyEvent e) {
		if (e.getID() != KeyEvent.KEY_PRESSED) {
			return;
		}
		if (!keyboardShortCut.matches(e)) {
			return;
		}
		Point currentMouseLocationOnScreen = MouseInfo.getPointerInfo().getLocation();
		if (currentMouseLocationOnScreen.equals(lastObservedMouseLocationOnScreen)) {
			objectInspector.onInspectionEvent(lastObservedComponent, lastObservedMouseLocationOnComponent);
		}
	}

	private void onMouseEventDispatched(MouseEvent e) {
		lastObservedComponent = e.getComponent();
		lastObservedMouseLocationOnComponent = e.getPoint();
		lastObservedMouseLocationOnScreen = MouseInfo.getPointerInfo().getLocation();
	}
}
