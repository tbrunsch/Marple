package dd.kms.marple;

import javax.swing.*;

public class Test
{
	public static void main(String[] args) throws ClassNotFoundException {
		JFrame testFrame = new TestFrame();

		TestUtils.setupInspectionFramework(testFrame);

		testFrame.pack();
		testFrame.setVisible(true);
	}
}
