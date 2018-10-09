package com.AMS.jBEAM.objectInspection;

import com.AMS.jBEAM.objectInspection.swing.gui.SwingComponentInspectionPanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Base class for all inspection frameworks
 */
public abstract class ObjectInspector
{
    private static ObjectInspector INSPECTOR   = null;

    private static void register(ObjectInspector inspector) {
        INSPECTOR = inspector;
    }

    private final Map<Class<?>, BiFunction<Object, MouseLocation, List<Object>>>    subComponentHierarchyStrategyByClass    = new HashMap<>();

    public synchronized static ObjectInspector getInspector() {
        if (INSPECTOR == null) {
            throw new IllegalStateException("No object inspector has been loaded");
        }
        return INSPECTOR;
    }

    public abstract void                    runLater(Runnable runnable);
    protected abstract Collection<Class<?>> getRegisteredClasses();
    protected abstract void                 beginInspection();
    protected abstract void                 endInspection();
    protected abstract void                 inspect(Object component, List<Object> subComponentHierarchy, MouseLocation mouseLocation);
    protected abstract void                 inspectAs(Object object, Class<?> clazz);

    protected synchronized void registerInspector(ObjectInspector inspector) {
        if (INSPECTOR == null) {
            INSPECTOR = inspector;
        } else if (inspector != INSPECTOR) {
            throw new IllegalStateException("There is already an object inspector loaded");
        }
    }

    protected synchronized void addSubcomponentHierarchyStrategy(Class<?> clazz, BiFunction<Object, MouseLocation, List<Object>> strategy) {
        if (subComponentHierarchyStrategyByClass.containsKey(clazz)) {
            // Warning: Already registered strategy for that component
            return;
        }
        subComponentHierarchyStrategyByClass.put(clazz, strategy);
    }

    /*
     * Inspection History
     */
    private final List<InspectionData>  inspectionHistory       = new ArrayList<>();
    private int                         inspectionHistoryIndex  = -1;

    public final boolean canInspectPrevious() {
        return inspectionHistoryIndex - 1 >= 0;
    }

    public final boolean canInspectNext() {
        return inspectionHistoryIndex + 1 < inspectionHistory.size();
    }

    public final void inspectPrevious() {
        if (canInspectPrevious()) {
            InspectionData inspectionData = inspectionHistory.get(--inspectionHistoryIndex);
            inspect(inspectionData.getObject(), inspectionData.getMouseLocation());
        }
    }

    public final void inspectNext() {
        if (canInspectNext()) {
            InspectionData inspectionData = inspectionHistory.get(++inspectionHistoryIndex);
            inspect(inspectionData.getObject(), inspectionData.getMouseLocation());
        }
    }

    public final void inspectObject(Object object) {
        inspect(new InspectionData(object, Optional.empty()));
    }

    public final void inspectComponent(Object component, MouseLocation mouseLocation) {
        inspect(new InspectionData(component, Optional.of(mouseLocation)));
    }

    final protected void clearInspectionHistory() {
        inspectionHistory.clear();
        inspectionHistoryIndex = -1;
    }

    final void inspect(InspectionData inspectionData) {
        if (canInspectNext()) {
            inspectionHistory.subList(inspectionHistoryIndex + 1, inspectionHistory.size()).clear();
        }
        inspectionHistory.add(inspectionData);
        inspectionHistoryIndex++;
        inspect(inspectionData.getObject(), inspectionData.getMouseLocation());
    }

    private synchronized void inspect(Object object, Optional<MouseLocation> mouseLocation) {
        runLater(() -> {
            beginInspection();

            final Object objectToInspect;
            if (mouseLocation.isPresent()) {
                MouseLocation location = mouseLocation.get();
                List<Object> subComponentHierarchy = getSubComponentHierarchy(object, location);
                inspect(object, subComponentHierarchy, location);
                objectToInspect = subComponentHierarchy.isEmpty() ? object : subComponentHierarchy.get(subComponentHierarchy.size()-1);
            } else {
                objectToInspect = object;
            }

            Set<Class<?>> registeredClasses = new HashSet<>(getRegisteredClasses());
            InspectionUtils.getImplementedClasses(objectToInspect, true).stream()
                    .filter(registeredClasses::contains)
                    .forEach(clazz -> inspectAs(objectToInspect, clazz));

            endInspection();
        });
    }

    private List<Object> getSubComponentHierarchy(Object component, MouseLocation mouseLocation) {
        Iterable<Class<?>> implementedClasses = InspectionUtils.getImplementedClasses(component, false);
        for (Class<?> clazz : implementedClasses) {
            BiFunction<Object, MouseLocation, List<Object>> strategy = subComponentHierarchyStrategyByClass.get(clazz);
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

    private static class InspectionData
    {
        private final Object                    object;
        private final Optional<MouseLocation>   mouseLocation;   // only relevant for GUI components

        InspectionData(Object object, Optional<MouseLocation> mouseLocation) {
            this.object = object;
            this.mouseLocation = mouseLocation;
        }

        Object getObject() {
            return object;
        }

        Optional<MouseLocation> getMouseLocation() {
            return mouseLocation;
        }
    }
}
