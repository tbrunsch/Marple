package com.AMS.jBEAM.objectInspection;

class ObjectInspectionStrategy extends AbstractInspectionStrategy
{
    private final Object object;

    ObjectInspectionStrategy(Object object) {
        this.object = object;
    }

    @Override
    public Object getObjectToInspect() {
        return object;
    }

    @Override
    void inspect(ObjectInspector inspector) {
        Object objectToInspect = getObjectToInspect();
        inspector.beginInspection(objectToInspect);
        inspector.inspectObject(objectToInspect);
        inspector.endInspection();
    }
}
