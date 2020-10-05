package dd.kms.marple.impl.gui.table;

import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.zenodot.api.wrappers.MemberInfo;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MemberInfoRenderer extends JLabel implements TableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setText(value.toString());
		MemberInfo memberInfo = (MemberInfo) value;
		Color color = GuiCommons.getAccessModifierColor(memberInfo.getAccessModifier());
		setFont(getFont().deriveFont(0));
		setBackground(color == null ? Color.BLACK : color);
		setOpaque(true);
		return this;
	}
}
