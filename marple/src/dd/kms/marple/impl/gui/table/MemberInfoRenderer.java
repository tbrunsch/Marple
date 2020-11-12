package dd.kms.marple.impl.gui.table;

import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.wrappers.MemberInfo;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MemberInfoRenderer extends JLabel implements TableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		MemberInfo memberInfo = (MemberInfo) value;
		AccessModifier accessModifier = memberInfo.getAccessModifier();
		setText(accessModifier.toString());
		Color color = GuiCommons.getAccessModifierColor(accessModifier);
		setFont(getFont().deriveFont(0));
		setBackground(color == null ? Color.BLACK : color);
		setOpaque(true);
		return this;
	}
}
