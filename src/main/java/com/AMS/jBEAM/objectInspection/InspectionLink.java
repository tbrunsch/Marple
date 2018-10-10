package com.AMS.jBEAM.objectInspection;

public class InspectionLink
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

    public void execute() {
        ObjectInspector inspector = ObjectInspector.getInspector();
        InspectionStrategyIF objectInspectionStrategy = inspector.createObjectInspectionStrategy(objectToInspect);
        inspector.inspect(objectInspectionStrategy);
    }

    @Override
    public String toString() {
        return getLinkText();
    }
}
