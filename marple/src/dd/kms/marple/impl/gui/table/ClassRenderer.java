package dd.kms.marple.impl.gui.table;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.common.GuiCommons;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ClassRenderer extends JLabel implements TableCellRenderer
{
	private final InspectionContext	context;

	public ClassRenderer(InspectionContext context) {
		this.context = context;
		setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setText(context.getDisplayText(value));
		setFont(getFont().deriveFont(Font.PLAIN));
		if (isSelected) {
			setForeground(GuiCommons.getSelectedTableForegroundColor());
			setBackground(GuiCommons.getSelectedTableBackgroundColor());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		return this;
	}
}
