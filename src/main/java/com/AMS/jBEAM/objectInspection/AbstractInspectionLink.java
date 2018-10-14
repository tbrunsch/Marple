package com.AMS.jBEAM.objectInspection;

import com.AMS.jBEAM.objectInspection.swing.SwingObjectInspector;

abstract class AbstractInspectionLink implements InspectionLinkIF
{
    private final String linkText;

    AbstractInspectionLink(String linkText) {
        this.linkText = linkText;
    }

    abstract void doInspect(ObjectInspector inspector);

    @Override
    public void run() {
        SwingObjectInspector.getInspector().inspect(this);
    }

    @Override
    public String toString() {
        return linkText;
    }

    @Override
    public final void inspect(ObjectInspector inspector) {
        inspector.runLater(() -> doInspect(inspector));
    }
}
