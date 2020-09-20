package dd.kms.marple.impl.gui.actionproviders;

import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNode;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Function;

public class ActionProviderListeners
{
	public static void addMouseListeners(JComponent component, Function<MouseEvent, ActionProvider> actionProviderFunction) {
		component.addMouseListener(new AbstractActionProviderMouseListener() {
			@Override
			protected ActionProvider getActionProvider(MouseEvent e) {
				return actionProviderFunction.apply(e);
			}
		});

		component.addMouseMotionListener(new AbstractActionProviderMouseMotionListener() {
			@Override
			protected ActionProvider getActionProvider(MouseEvent e) {
				return actionProviderFunction.apply(e);
			}
		});
	}

	public static void addMouseListeners(JTable table) {
		Function<MouseEvent, ActionProvider> actionProviderFunction = e -> getTableCellValueAsActionProvider(table, e.getPoint());
		addMouseListeners(table, actionProviderFunction);
	}

	public static void addMouseListeners(JTree tree) {
		Function<MouseEvent, ActionProvider> actionProviderFunction = e -> getTreeNodeAsActionProvider(tree, e.getPoint());
		addMouseListeners(tree, actionProviderFunction);
	}

	private static ActionProvider getTableCellValueAsActionProvider(JTable table, Point p) {
		TableCellCoordinates modelCoordinates = getTableModelCoordinates(table, p);
		if (modelCoordinates != null) {
			int row = modelCoordinates.getRow();
			int col = modelCoordinates.getColumn();
			if (row >= 0 && col >= 0) {
				TableModel tableModel = table.getModel();
				Object cellValue = tableModel.getValueAt(row, col);
				if (cellValue instanceof ActionProvider) {
					return (ActionProvider) cellValue;
				}
			}
		}
		return null;
	}

	private static TableCellCoordinates getTableModelCoordinates(JTable table, Point p) {
		int row = table.rowAtPoint(p);
		int col = table.columnAtPoint(p);
		if (row < 0 || col < 0) {
			return null;
		}
		return new TableCellCoordinates(table.convertRowIndexToModel(row), table.convertColumnIndexToModel(col));
	}

	private static class TableCellCoordinates
	{
		private final int row;
		private final int column;

		TableCellCoordinates(int row, int col) {
			this.row = row;
			this.column = col;
		}

		int getRow() {
			return row;
		}

		int getColumn() {
			return column;
		}
	}


	private static ActionProvider getTreeNodeAsActionProvider(JTree tree, Point p) {
		TreePath path = tree.getPathForLocation(p.x, p.y);
		if (path == null) {
			return null;
		}
		Object node = path.getLastPathComponent();
		return node instanceof ActionProviderTreeNode
			? ((ActionProviderTreeNode) node).getActionProvider()
			: null;
	}
}
