package com.AMS.jBEAM.objectInspection;

public class InspectionLink implements Runnable
{
    private final Object objectToInspect;
    private final String linkText;

    public InspectionLink(Object objectToInspect, String linkText) {
        this.objectToInspect = objectToInspect;
        this.linkText = linkText;
    }

    public String getLinkText() {
        return linkText;
    }

    @Override
    public void run() {
        ObjectInspector inspector = ObjectInspector.getInspector();
        InspectionStrategyIF objectInspectionStrategy = inspector.createObjectInspectionStrategy(objectToInspect);
        inspector.inspect(objectInspectionStrategy);
    }

    @Override
    public String toString() {
        return getLinkText();
    }
}
