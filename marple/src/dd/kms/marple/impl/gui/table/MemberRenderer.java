package dd.kms.marple.impl.gui.table;

import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.zenodot.api.common.AccessModifier;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.lang.reflect.Member;
import java.util.Objects;

public class MemberRenderer extends JLabel implements TableCellRenderer
{
	public MemberRenderer() {
		setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Member member = (Member) value;
		AccessModifier accessModifier = AccessModifier.getValue(member.getModifiers());
		setText(accessModifier.toString());
		Color bg = GuiCommons.getAccessModifierColor(accessModifier);
		setFont(getFont().deriveFont(Font.PLAIN));
		Color fg = table.getForeground();
		if (isSelected) {
			setForeground(GuiCommons.getSelectedTableForegroundColor(fg));
			setBackground(GuiCommons.getSelectedTableBackgroundColor(bg));
		} else {
			setForeground(fg);
			setBackground(bg);
		}
		return this;
	}
}
