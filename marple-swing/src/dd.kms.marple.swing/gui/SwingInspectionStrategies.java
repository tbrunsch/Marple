package dd.kms.marple.swing.gui;

import dd.kms.marple.swing.SwingObjectInspector;

public class SwingInspectionStrategies
{
	public static void register() {
		SwingObjectInspector.getInspector().addInspectionStrategyFor(Object.class, object -> new SwingInspectionViewData("Fields", new SwingFieldInspectionPanel(object)));
		SwingObjectInspector.getInspector().addInspectionStrategyFor(Object.class, object -> new SwingInspectionViewData("Methods", new SwingMethodInspectionPanel(object)));
	}
}
