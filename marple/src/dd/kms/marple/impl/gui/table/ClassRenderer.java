package dd.kms.marple.impl.gui.table;

import dd.kms.marple.api.InspectionContext;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ClassRenderer extends JLabel implements TableCellRenderer
{
	private final InspectionContext	context;

	public ClassRenderer(InspectionContext context) {
		this.context = context;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setText(context.getDisplayText(value));
		setFont(getFont().deriveFont(0));
		return this;
	}
}
