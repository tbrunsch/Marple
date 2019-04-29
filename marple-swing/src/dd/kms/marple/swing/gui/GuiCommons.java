package dd.kms.marple.swing.gui;

import javax.swing.*;
import java.awt.*;

public class GuiCommons {
	public static void showPanel(String title, JPanel panel) {
		JDialog dialog = new JDialog(null, title, Dialog.ModalityType.APPLICATION_MODAL);
		dialog.getContentPane().add(panel);
		dialog.pack();
		dialog.setVisible(true);
	}
}
