package dd.kms.marple.swing;

import dd.kms.marple.settings.InspectionSettings;

import javax.swing.*;
import java.awt.*;

public class Test
{
    public static void main(String[] args) {
        InspectionSettings<Component, Component, SwingKey, Point> inspectionSettings = SwingObjectInspectionFramework.createInspectionSettingsBuilder().build();
        SwingObjectInspectionFramework.register("Test", inspectionSettings);

        JFrame test = new TestFrame();
        test.pack();
        test.setVisible(true);
    }
}
