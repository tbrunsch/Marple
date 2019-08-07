package dd.kms.marple.gui.table;

import dd.kms.marple.gui.filters.ValueFilter;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class FilterPopupPanel extends JPanel
{
	private final JLabel	titleLabel;
	private final Component	editor;

	FilterPopupPanel(ValueFilter valueFilter, String columnName) {
		super(new GridBagLayout());

		this.titleLabel = new JLabel("Filter (" + columnName + ")");
		this.editor = valueFilter.getEditor();

		add(titleLabel,	new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(editor,		new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		SwingUtilities.invokeLater(editor::requestFocusInWindow);
	}
}
