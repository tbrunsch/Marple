package dd.kms.marple.swing.gui.table;

import dd.kms.marple.actions.ActionProvider;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ActionProviderRenderer extends JLabel implements TableCellRenderer
{
	private static final Color	DARK_BLUE	= Color.BLUE.darker();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setText(value == null ? null : value.toString());
		setFont(getFont().deriveFont(0));
		setForeground(value instanceof ActionProvider ? DARK_BLUE : Color.BLACK);
		return this;
	}
}
