package dd.kms.marple.gui.table;

import dd.kms.marple.gui.common.GuiCommons;
import dd.kms.zenodot.api.common.AccessModifier;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class AccessModifierRenderer extends JLabel implements TableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setText(value.toString());
		Color color = GuiCommons.getAccessModifierColor((AccessModifier) value);
		setFont(getFont().deriveFont(0));
		setBackground(color == null ? Color.BLACK : color);
		setOpaque(true);
		return this;
	}
}
