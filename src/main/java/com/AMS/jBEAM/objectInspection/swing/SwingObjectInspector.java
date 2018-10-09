package com.AMS.jBEAM.objectInspection.swing;

import com.AMS.jBEAM.objectInspection.ObjectInspector;
import com.AMS.jBEAM.objectInspection.InspectionUtils;
import com.AMS.jBEAM.objectInspection.MouseLocation;
import com.AMS.jBEAM.objectInspection.swing.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SwingObjectInspector extends ObjectInspector
{
    private static final SwingKeyboardShortCut  DEBUG_SHORT_CUT = new SwingKeyboardShortCut(KeyEvent.VK_I, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);

    private static SwingObjectInspector         INSPECTOR       = null;

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

    private final Map<Class<?>, Function<Object, SwingInspectionViewData>>  inspectionStrategyByClass               = new HashMap<>();

    private SwingInspectionFrame                                            swingInspectionFrame                    = null;

    /*
     * Setup Code
     */
    SwingObjectInspector() {
        registerInspector(this);
        SwingEventHandler.register(this, DEBUG_SHORT_CUT);
    }

    /*
     * Specify how to present data of instances of type T in a JComponent
     */
    public synchronized <T> void addInspectionStrategyFor(Class<T> clazz, Function<T, SwingInspectionViewData> strategy) {
        if (inspectionStrategyByClass.containsKey(clazz)) {
            // Warning: Already registered strategy for that class
            return;
        }
        Function<Object, SwingInspectionViewData> wrappedStrategy = object -> strategy.apply(clazz.cast(object));
        inspectionStrategyByClass.put(clazz, wrappedStrategy);
    }

    /*
     * Specify a hierarchy of sub components for a given component and a specific location that are no components.
     * This may be, e.g., a TreeNode of a JTree, but also a more complex hierarchy that is not represented
     * via JComponents.
     */
    public <T extends Component> void addSubcomponentHierarchyStrategyFor(Class<T> clazz, BiFunction<T, Point, List<Object>> strategy) {
        BiFunction<Object, MouseLocation, List<Object>> wrappedStrategy = (object, mouseLocation) -> strategy.apply(clazz.cast(object), toPoint(mouseLocation));
        addSubcomponentHierarchyStrategy(clazz, wrappedStrategy);
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
        return inspectionStrategyByClass.keySet();
    }

    @Override
    protected void beginInspection() {
        if (swingInspectionFrame == null) {
            swingInspectionFrame = new SwingInspectionFrame(this::onInspectionFrameClosed);
        }
        swingInspectionFrame.removeAllComponents();
    }

    @Override
    protected void endInspection() {
        swingInspectionFrame.revalidate();
        swingInspectionFrame.repaint();
        swingInspectionFrame.setVisible(true);
    }

    @Override
    protected void inspectAs(Object object, Class<?> clazz) {
        Function<Object, SwingInspectionViewData> strategy = inspectionStrategyByClass.get(clazz);
        if (strategy != null) {
            SwingInspectionViewData inspectionViewData = strategy.apply(object);
            swingInspectionFrame.addComponent(inspectionViewData.getTitle(), inspectionViewData.getComponent());
        }
    }

    /*
     * Component Inspection
     */
    @Override
    protected void inspect(Object componentAsObject, List<Object> subComponentHierarchy, MouseLocation mouseLocation) {
        if (!(componentAsObject instanceof Component)) {
            return;
        }
        Component component = (Component) componentAsObject;
        JComponent panel = new SwingComponentInspectionPanel(component, subComponentHierarchy);
        swingInspectionFrame.addComponent("Component Hierarchy", panel);
    }

    private static MouseLocation toMouseLocation(Point p) {
        return new MouseLocation(p.x, p.y);
    }

    private static Point toPoint(MouseLocation mouseLocation) {
        return new Point(mouseLocation.getX(), mouseLocation.getY());
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
        inspectComponent(component, toMouseLocation(point));
    }

    private void onInspectionFrameClosed() {
        swingInspectionFrame = null;
        clearInspectionHistory();
    }
}
