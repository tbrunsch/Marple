package dd.kms.marple.swing;

import dd.kms.marple.AbstractObjectInspectionFramework;
import dd.kms.marple.components.ComponentHierarchyModelBuilder;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.settings.InspectionSettings;
import dd.kms.marple.settings.InspectionSettingsBuilder;
import dd.kms.marple.settings.InspectionSettingsBuilders;
import dd.kms.marple.swing.evaluator.SwingExpressionEvaluator;
import dd.kms.marple.swing.gui.SubcomponentHierarchyStrategies;
import dd.kms.marple.swing.inspector.SwingObjectInspector;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.security.AccessControlException;

public class SwingObjectInspectionFramework extends AbstractObjectInspectionFramework<Component, SwingKey, Point>
{
	private static final SwingKey	INSPECTION_KEY	= new SwingKey(KeyEvent.VK_I, 	KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
	private static final SwingKey	EVALUATION_KEY	= new SwingKey(KeyEvent.VK_F8,	KeyEvent.ALT_DOWN_MASK | KeyEvent.ALT_MASK);

	private static final Object						LOCK		= new Object();
	private static SwingObjectInspectionFramework	FRAMEWORK	= null;

	public static InspectionSettingsBuilder<Component, SwingKey, Point> createInspectionSettingsBuilder() {
		return InspectionSettingsBuilders.<Component, SwingKey, Point>create(Component.class)
			.inspector(new SwingObjectInspector())
			.inspectionKey(INSPECTION_KEY)
			.evaluator(new SwingExpressionEvaluator())
			.evaluationKey(EVALUATION_KEY)
			.componentHierarchyModel(createComponentHierarchyModelBuilder().build());
	}

	public static ComponentHierarchyModelBuilder<Component, Point> createComponentHierarchyModelBuilder() {
		ComponentHierarchyModelBuilder<Component, Point> builder = ComponentHierarchyModels.createBuilder(Component::getParent);
		SubcomponentHierarchyStrategies.addDefaultSubcomponentStrategies(builder);
		return builder;
	}

	public static void register(Object identifier, InspectionSettings<Component, SwingKey, Point> inspectionSettings) {
		synchronized (LOCK) {
			SwingObjectInspectionFramework framework = getFramework(true);
			framework.registerSettings(identifier, inspectionSettings);
		}
	}

	public static void unregister(Object identifier) {
		synchronized (LOCK) {
			SwingObjectInspectionFramework framework = getFramework(false);
			if (framework != null) {
				framework.unregisterSettings(identifier);
			}
		}
	}

	private static SwingObjectInspectionFramework getFramework(boolean createIfRequired) {
		if (FRAMEWORK == null && createIfRequired) {
			FRAMEWORK = new SwingObjectInspectionFramework();
		}
		return FRAMEWORK;
	}

	@Override
	protected void registerListeners() {
		if (canRegisterListeners()) {
			long eventMask = AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;
			Toolkit.getDefaultToolkit().addAWTEventListener(this::eventDispatched, eventMask);
		}
	}

	@Override
	protected void unregisterListeners() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(this::eventDispatched);
	}

	@Override
	protected Point getMousePositionOnScreen() {
		return MouseInfo.getPointerInfo().getLocation();
	}

	@Override
	protected boolean keyMatches(SwingKey actualKey, SwingKey expectedKey) {
		return actualKey.matches(expectedKey);
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
			onKeyPressed(new SwingKey(e.getKeyCode(), e.getModifiers()));
		}
	}

	private void handleMouseOver(Component component, Point point) {
		if (component instanceof Container) {
			component = ((Container) component).findComponentAt(point);
		}
		onMouseOverComponentAction(component, point);
	}
}
