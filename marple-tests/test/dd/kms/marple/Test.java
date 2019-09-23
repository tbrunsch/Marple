package dd.kms.marple;

import javax.swing.*;

public class Test
{
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame testFrame = new TestFrame();

			try {
				TestUtils.setupInspectionFramework(testFrame);
			} catch (ClassNotFoundException ignored) {
			}

			testFrame.pack();
			testFrame.setVisible(true);
		});
	}
}
