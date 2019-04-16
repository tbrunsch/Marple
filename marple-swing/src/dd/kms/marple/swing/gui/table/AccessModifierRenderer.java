package dd.kms.marple.swing.gui.table;

import dd.kms.marple.common.AccessModifier;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AccessModifierRenderer extends JLabel implements TableCellRenderer
{
	private static final Map<AccessModifier, Color> MODIFIER_COLORS = new HashMap<>();

	static {
		MODIFIER_COLORS.put(AccessModifier.PUBLIC,			Color.GREEN);
		MODIFIER_COLORS.put(AccessModifier.PROTECTED,		Color.YELLOW);
		MODIFIER_COLORS.put(AccessModifier.PACKAGE_PRIVATE,	Color.ORANGE);
		MODIFIER_COLORS.put(AccessModifier.PRIVATE,			Color.RED);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setText(value.toString());
		Color color = MODIFIER_COLORS.get(value);
		setFont(getFont().deriveFont(0));
		setBackground(color == null ? Color.BLACK : color);
		setOpaque(true);
		return this;
	}
}
