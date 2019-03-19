package dd.kms.marple;

import dd.kms.marple.swing.SwingObjectInspector;

import javax.swing.*;

public class Test
{
    public static void main(String[] args) {
        SwingObjectInspector.load();

        JFrame test = new TestFrame();
        test.pack();
        test.setVisible(true);
    }
}
