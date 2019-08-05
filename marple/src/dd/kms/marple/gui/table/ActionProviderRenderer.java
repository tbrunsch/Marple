package dd.kms.marple.gui.table;

import dd.kms.marple.actions.ActionProvider;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ActionProviderRenderer implements TableCellRenderer
{
	private static final Color	DARK_BLUE	= Color.BLUE.darker();

	private final TableCellRenderer	wrappedRenderer	= new DefaultTableCellRenderer();

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel rendererComponent = (JLabel) wrappedRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		rendererComponent.setText(value == null ? null : value.toString());
		rendererComponent.setFont(rendererComponent.getFont().deriveFont(0));
		rendererComponent.setForeground(value instanceof ActionProvider ? DARK_BLUE : Color.BLACK);
		return rendererComponent;
	}
}
