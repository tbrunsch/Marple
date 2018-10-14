package com.AMS.jBEAM.objectInspection;

public interface InspectionLinkIF extends Runnable
{
    Object getObjectToInspect();

    // Should only be called by ObjectInspector
    void inspect(ObjectInspector inspector);
}
