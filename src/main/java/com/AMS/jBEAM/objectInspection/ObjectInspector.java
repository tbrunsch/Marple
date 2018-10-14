package com.AMS.jBEAM.objectInspection;

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
    protected abstract void                 beginInspection(Object object);
    protected abstract void                 endInspection();
    protected abstract void                 inspectComponent(Object component, List<Object> subComponentHierarchy);
    protected abstract void                 inspectObjectAs(Object object, Class<?> clazz);

    // Should only be called by InspectionStrategyIFs
    void inspectObject(Object object) {
        Set<Class<?>> registeredClasses = new HashSet<>(getRegisteredClasses());
        InspectionUtils.getImplementedClasses(object, true).stream()
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

    public InspectionLinkIF createComponentInspectionStrategy(Object component, MouseLocation mouseLocation) {
        List<Object> subComponentHierarchy = getSubComponentHierarchy(component, mouseLocation);
        return createComponentInspectionStrategy(component, subComponentHierarchy);
    }

    public InspectionLinkIF createComponentInspectionStrategy(Object component, List<Object> subComponentHierarchy) {
        return new ComponentInspectionLink(component, subComponentHierarchy);
    }

    public InspectionLinkIF createObjectInspectionLink(Object object) {
        return new ObjectInspectionLink(object);
    }

    public InspectionLinkIF createObjectInspectionLink(Object object, String linkText) {
        return new ObjectInspectionLink(object, linkText);
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
}
