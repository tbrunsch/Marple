package dd.kms.marple;

import javax.swing.*;

public class TestMemoryLeakDetection
{
	public static void main(String[] args) {
		JFrame testFrame = new MemoryLeakTestFrame();

		TestUtils.setupInspectionFramework(testFrame);

		testFrame.pack();
		testFrame.setVisible(true);
	}
}
