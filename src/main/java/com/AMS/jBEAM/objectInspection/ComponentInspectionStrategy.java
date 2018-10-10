package com.AMS.jBEAM.objectInspection;

import java.util.List;

class ComponentInspectionStrategy extends AbstractInspectionStrategy
{
    private final Object        component;
    private final List<Object>  subComponentHierarchy;

    ComponentInspectionStrategy(Object component, List<Object> subComponentHierarchy) {
        this.component = component;
        this.subComponentHierarchy = subComponentHierarchy;
    }

    @Override
    public Object getObjectToInspect() {
        return subComponentHierarchy.isEmpty() ? component : subComponentHierarchy.get(subComponentHierarchy.size()-1);
    }

    @Override
    void inspect(ObjectInspector inspector) {
        Object objectToInspect = getObjectToInspect();
        inspector.beginInspection(objectToInspect);
        inspector.inspectComponent(component, subComponentHierarchy);
        inspector.inspectObject(objectToInspect);
        inspector.endInspection();
    }
}
