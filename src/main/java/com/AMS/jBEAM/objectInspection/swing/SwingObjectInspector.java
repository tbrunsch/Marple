package com.AMS.jBEAM.objectInspection.swing;

import com.AMS.jBEAM.objectInspection.InspectionLinkIF;
import com.AMS.jBEAM.javaParser.ReflectionUtils;
import com.AMS.jBEAM.objectInspection.MouseLocation;
import com.AMS.jBEAM.objectInspection.ObjectInspector;
import com.AMS.jBEAM.objectInspection.swing.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SwingObjectInspector extends ObjectInspector
{
	private static final SwingKeyboardShortCut	DEBUG_SHORT_CUT	= new SwingKeyboardShortCut(KeyEvent.VK_I, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);

	private static SwingObjectInspector			INSPECTOR		= null;

	public synchronized static void load() {
		if (INSPECTOR == null) {
			INSPECTOR = new SwingObjectInspector();
			SwingInspectionStrategies.register();
			SwingSubComponentHierarchyStrategies.register();
		}
	}

	public static SwingObjectInspector getInspector() {
		load();
		return INSPECTOR;
	}

	private final Map<Class<?>, List<Function<Object, SwingInspectionViewData>>>	inspectionStrategiesByClass			   = new HashMap<>();
	private final Map<Class<?>, BiFunction<Component, MouseLocation, List<Object>>> subComponentHierarchyStrategyByClass	= new HashMap<>();

	private SwingInspectionFrame													swingInspectionFrame					= null;

	/*
	 * Setup Code
	 */
	private SwingObjectInspector() {
		registerInspector(this);
		SwingEventHandler.register(this, DEBUG_SHORT_CUT);
	}

	/*
	 * Specify how to present data of instances of type T in a JComponent
	 */
	public synchronized <T> void addInspectionStrategyFor(Class<T> clazz, Function<T, SwingInspectionViewData> strategy) {
		if (!inspectionStrategiesByClass.containsKey(clazz)) {
			inspectionStrategiesByClass.put(clazz, new ArrayList<>());
		}
		List<Function<Object, SwingInspectionViewData>> strategies = inspectionStrategiesByClass.get(clazz);
		Function<Object, SwingInspectionViewData> wrappedStrategy = object -> strategy.apply(clazz.cast(object));
		strategies.add(wrappedStrategy);
	}

	/*
	 * Specify a hierarchy of sub components for a given component and a specific location that are no components.
	 * This may be, e.g., a TreeNode of a JTree, but also a more complex hierarchy that is not represented
	 * via JComponents.
	 */
	public <T extends Component> void addSubcomponentHierarchyStrategyFor(Class<T> clazz, BiFunction<T, Point, List<Object>> strategy) {
		BiFunction<Component, MouseLocation, List<Object>> wrappedStrategy = (object, mouseLocation) -> strategy.apply(clazz.cast(object), toPoint(mouseLocation));
		addSubcomponentHierarchyStrategy(clazz, wrappedStrategy);
	}

	private synchronized void addSubcomponentHierarchyStrategy(Class<? extends Component> clazz, BiFunction<Component, MouseLocation, List<Object>> strategy) {
		if (subComponentHierarchyStrategyByClass.containsKey(clazz)) {
			// Warning: Already registered strategy for that component
			return;
		}
		subComponentHierarchyStrategyByClass.put(clazz, strategy);
	}

	private List<Object> getSubComponentHierarchy(Component component, MouseLocation mouseLocation) {
		Iterable<Class<?>> implementedClasses = ReflectionUtils.getImplementedClasses(component, false);
		for (Class<?> clazz : implementedClasses) {
			BiFunction<Component, MouseLocation, List<Object>> strategy = subComponentHierarchyStrategyByClass.get(clazz);
			if (strategy == null) {
				continue;
			}
			List<Object> subComponentHierarchy = strategy.apply(component, mouseLocation);
			if (!subComponentHierarchy.isEmpty()) {
				return subComponentHierarchy;
			}
		}
		return Collections.emptyList();
	}

	/*
	 * Implementation of Template Methods
	 */
	@Override
	public void runLater(Runnable runnable) {
		SwingUtilities.invokeLater(runnable);
	}

	@Override
	protected Collection<Class<?>> getRegisteredClasses() {
		return inspectionStrategiesByClass.keySet();
	}

	@Override
	protected void beginInspection(Object object) {
		if (swingInspectionFrame == null) {
			swingInspectionFrame = new SwingInspectionFrame(this::onInspectionFrameClosed);
		}
		swingInspectionFrame.beginInspection(object);
		swingInspectionFrame.removeAllComponents();
	}

	@Override
	protected void endInspection() {
		swingInspectionFrame.revalidate();
		swingInspectionFrame.repaint();
		swingInspectionFrame.endInspection();
	}

	@Override
	protected void inspectComponent(List<Object> componentHierarchy) {
		JComponent panel = new SwingComponentInspectionPanel(componentHierarchy);
		swingInspectionFrame.addComponent("Component Hierarchy", panel);
	}

	@Override
	protected void inspectObjectAs(Object object, Class<?> clazz) {
		List<Function<Object, SwingInspectionViewData>> strategies = inspectionStrategiesByClass.get(clazz);
		if (strategies == null) {
			return;
		}
		for (Function<Object, SwingInspectionViewData> strategy : strategies) {
			SwingInspectionViewData inspectionViewData = strategy.apply(object);
			swingInspectionFrame.addComponent(inspectionViewData.getTitle(), inspectionViewData.getComponent());
		}
	}

	/*
	 * Event Handling
	 */
	void onInspectionEvent(Component component, Point point) {
		// The component is not always the leaf in the component tree.
		// Use findComponentAt recursively to find the leaf component
		// at the specified location.
		if (component instanceof Container) {
			Point coordinatesInComponent = new Point(point);
			if (component.getParent() != null) {
				// Coordinates are given w.r.t. component's parent. Translate it to coordinates w.r.t. component.
				coordinatesInComponent.translate(-component.getX(), -component.getY());
			}
			Component nestedComponent = ((Container) component).findComponentAt(coordinatesInComponent);
			if (nestedComponent != null && nestedComponent != component) {
				onInspectionEvent(nestedComponent, coordinatesInComponent);
				return;
			}
		}
		MouseLocation mouseLocation = toMouseLocation(point);
		List<Object> componentHierarchy = SwingInspectionUtils.getComponentHierarchy(component);
		componentHierarchy.addAll(getSubComponentHierarchy(component, mouseLocation));
		InspectionLinkIF componentInspectionLink = createComponentInspectionLink(componentHierarchy);
		componentInspectionLink.run();
	}

	private void onInspectionFrameClosed() {
		swingInspectionFrame = null;
		clearInspectionHistory();
	}

	private static MouseLocation toMouseLocation(Point p) {
		return new MouseLocation(p.x, p.y);
	}

	private static Point toPoint(MouseLocation mouseLocation) {
		return new Point(mouseLocation.getX(), mouseLocation.getY());
	}
}
