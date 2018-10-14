package com.AMS.jBEAM.objectInspection;

import java.util.List;

class ComponentInspectionLink extends AbstractInspectionLink
{
    private static Object getLastObjectInHierarchy(Object component, List<Object> subComponentHierarchy) {
        return subComponentHierarchy.isEmpty() ? component : subComponentHierarchy.get(subComponentHierarchy.size()-1);
    }

    private final Object        component;
    private final List<Object>  subComponentHierarchy;

    ComponentInspectionLink(Object component, List<Object> subComponentHierarchy) {
        super(getLastObjectInHierarchy(component, subComponentHierarchy).toString());
        this.component = component;
        this.subComponentHierarchy = subComponentHierarchy;
    }

    @Override
    public Object getObjectToInspect() {
        return getLastObjectInHierarchy(component, subComponentHierarchy);
    }

    @Override
    void doInspect(ObjectInspector inspector) {
        Object objectToInspect = getObjectToInspect();
        inspector.beginInspection(objectToInspect);
        inspector.inspectComponent(component, subComponentHierarchy);
        inspector.inspectObject(objectToInspect);
        inspector.endInspection();
    }
}
