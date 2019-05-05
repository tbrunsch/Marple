package dd.kms.marple.gui.table;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class FilterPopup extends JDialog
{
	FilterPopup(TableValueFilter valueFilter, String columnName) {
		super((Frame) null, "Filter (" + columnName + ")");

		Component editor = valueFilter.getEditor();
		getContentPane().add(editor);

		setMinimumSize(new Dimension(200, 20));

		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		getRootPane().registerKeyboardAction(e -> dispose(), escape, JComponent.WHEN_IN_FOCUSED_WINDOW);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				FilterPopup.this.dispose();
			}
		});
	}
}
