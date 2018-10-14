package com.AMS.jBEAM.objectInspection;

class ObjectInspectionLink extends AbstractInspectionLink
{
    private final Object object;

    ObjectInspectionLink(Object object) {
        this(object, object.toString());
    }

    ObjectInspectionLink(Object object, String linkText) {
        super(linkText);
        this.object = object;
    }

    @Override
    public Object getObjectToInspect() {
        return object;
    }

    @Override
    void doInspect(ObjectInspector inspector) {
        Object objectToInspect = getObjectToInspect();
        inspector.beginInspection(objectToInspect);
        inspector.inspectObject(objectToInspect);
        inspector.endInspection();
    }
}
