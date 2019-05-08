package dd.kms.marple.gui.common;

import javax.swing.*;
import java.awt.*;

public class GuiCommons {
	public static void showInDialog(String title, JComponent component) {
		JDialog dialog = new JDialog(null, title, Dialog.ModalityType.APPLICATION_MODAL);
		dialog.getContentPane().add(component);
		dialog.pack();
		dialog.setVisible(true);
	}
}
