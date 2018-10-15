package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.swing.SwingObjectInspector;

public class SwingInspectionStrategies
{
    public static void register() {
        SwingObjectInspector.getInspector().addInspectionStrategyFor(Object.class, object -> new SwingInspectionViewData("Fields", new SwingFieldInspectionPanel(object)));
        SwingObjectInspector.getInspector().addInspectionStrategyFor(Object.class, object -> new SwingInspectionViewData("Methods", new SwingMethodInspectionPanel(object)));
    }
}
