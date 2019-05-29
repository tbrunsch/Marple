package dd.kms.marple.gui.table;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.awt.GridBagConstraints.*;

class FilterPopupPanel extends JPanel
{
	private static final Insets	DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	private final JLabel	titleLabel;
	private final Component	editor;

	FilterPopupPanel(TableValueFilter valueFilter, String columnName) {
		super(new GridBagLayout());

		this.titleLabel = new JLabel("Filter (" + columnName + ")");
		this.editor = valueFilter.getEditor();

		add(titleLabel,	new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(editor,		new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		SwingUtilities.invokeLater(editor::requestFocusInWindow);
	}
}
