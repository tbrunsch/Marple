package dd.kms.marple.impl.gui.table;

import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.zenodot.api.common.AccessModifier;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.lang.reflect.Member;

public class MemberRenderer extends JLabel implements TableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Member member = (Member) value;
		AccessModifier accessModifier = AccessModifier.getValue(member.getModifiers());
		setText(accessModifier.toString());
		Color color = GuiCommons.getAccessModifierColor(accessModifier);
		setFont(getFont().deriveFont(0));
		setBackground(color == null ? Color.BLACK : color);
		setOpaque(true);
		return this;
	}
}
