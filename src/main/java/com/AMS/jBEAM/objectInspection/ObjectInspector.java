package com.AMS.jBEAM.objectInspection;

import com.AMS.jBEAM.javaParser.ReflectionUtils;

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
    protected abstract void                 inspectComponent(List<Object> componentHierarchy);
    protected abstract void                 inspectObjectAs(Object object, Class<?> clazz);

    // Should only be called by InspectionStrategyIFs
    void inspectObject(Object object) {
        Set<Class<?>> registeredClasses = new HashSet<>(getRegisteredClasses());
        ReflectionUtils.getImplementedClasses(object, true).stream()
                .filter(registeredClasses::contains)
                .forEach(clazz -> inspectObjectAs(object, clazz));
    }

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
    private final List<InspectionLinkIF>    inspectionLinkHistory = new ArrayList<>();
    private int                             inspectionLinkHistoryIndex = -1;

    public final boolean canInspectPrevious() {
        return inspectionLinkHistoryIndex - 1 >= 0;
    }

    public final boolean canInspectNext() {
        return inspectionLinkHistoryIndex + 1 < inspectionLinkHistory.size();
    }

    public final void inspectPrevious() {
        if (canInspectPrevious()) {
            InspectionLinkIF inspectionLink = inspectionLinkHistory.get(--inspectionLinkHistoryIndex);
            inspectionLink.inspect(this);
        }
    }

    public final void inspectNext() {
        if (canInspectNext()) {
            InspectionLinkIF inspectionLink = inspectionLinkHistory.get(++inspectionLinkHistoryIndex);
            inspectionLink.inspect(this);
        }
    }

    public final void clearInspectionHistory() {
        inspectionLinkHistory.clear();
        inspectionLinkHistoryIndex = -1;
    }

    protected final void inspect(InspectionLinkIF inspectionLink) {
        if (canInspectNext()) {
            inspectionLinkHistory.subList(inspectionLinkHistoryIndex + 1, inspectionLinkHistory.size()).clear();
        }
        inspectionLinkHistory.add(inspectionLink);
        inspectionLinkHistoryIndex++;
        inspectionLink.inspect(this);
    }

    public InspectionLinkIF createComponentInspectionLink(List<Object> componentHierarchy) {
        return new ComponentInspectionLink(componentHierarchy);
    }

    public InspectionLinkIF createComponentInspectionLink(List<Object> componentHierarchy, String linkText) {
        return new ComponentInspectionLink(componentHierarchy, linkText);
    }

    public InspectionLinkIF createObjectInspectionLink(Object object) {
        return new ObjectInspectionLink(object);
    }
}
