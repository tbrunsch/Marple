package com.AMS.jBEAM.objectInspection;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Base class for all inspection frameworks
 */
public abstract class ObjectInspector
{
    private static ObjectInspector INSPECTOR   = null;

    private static void register(ObjectInspector inspector) {
        INSPECTOR = inspector;
    }

    public synchronized static ObjectInspector getInspector() {
        if (INSPECTOR == null) {
            throw new IllegalStateException("No object inspector has been loaded");
        }
        return INSPECTOR;
    }

    public abstract void                    runLater(Runnable runnable);
    protected abstract Collection<Class<?>> getRegisteredClasses();
    protected abstract void                 beginInspection(Object object);
    protected abstract void                 endInspection();
    protected abstract void                 inspect(Object component, MouseLocation mouseLocation);
    protected abstract void                 inspectAs(Object object, Class<?> clazz);

    protected synchronized void registerInspector(ObjectInspector inspector) {
        if (INSPECTOR == null) {
            INSPECTOR = inspector;
        } else if (inspector != INSPECTOR) {
            throw new IllegalStateException("There is already an object inspector loaded");
        }
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
            beginInspection(object);

            if (mouseLocation.isPresent()) {
                inspect(object, mouseLocation.get());
            }

            Set<Class<?>> registeredClasses = new HashSet<>(getRegisteredClasses());
            InspectionUtils.getImplementedClasses(object, true).stream()
                    .filter(registeredClasses::contains)
                    .forEach(clazz -> inspectAs(object, clazz));

            endInspection();
        });
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
