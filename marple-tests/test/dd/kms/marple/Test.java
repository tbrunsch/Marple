package dd.kms.marple;

import dd.kms.marple.settings.InspectionSettings;

import javax.swing.*;

public class Test
{
	public static void main(String[] args) {
		InspectionSettings inspectionSettings = ObjectInspectionFramework.createInspectionSettingsBuilder().build();
		ObjectInspectionFramework.register("Test", inspectionSettings);

		JFrame test = new TestFrame();
		test.pack();
		test.setVisible(true);
	}
}
