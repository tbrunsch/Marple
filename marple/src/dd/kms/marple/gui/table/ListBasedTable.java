package dd.kms.marple.gui.table;

import dd.kms.marple.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.gui.filters.ValueFilter;
import dd.kms.marple.gui.filters.ValueFilters;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A table whose rows values only depend on a 1-dimensional list.
 * Which value is displayed in which row is defined by the
 * corresponding {@link ColumnDescription}.
 *
 * @param <T>
 */
public class ListBasedTable<T> extends JPanel
{
	private final ListBasedTableModel<T>	tableModel;
	private final TableRowSorter			rowSorter;
	private final JTable					table;
	private final JScrollPane				scrollPane;

	public ListBasedTable(List<T> list, List<ColumnDescription<T>> columnDescriptions) {
		super(new BorderLayout());

		tableModel = new ListBasedTableModel<>(list, columnDescriptions);

		RowFilter<ListBasedTableModel<?>, Integer> rowFilter = new ListBasedTableRowFilter();
		rowSorter = new TableRowSorter<>(tableModel);
		rowSorter.setRowFilter(rowFilter);

		table = new JTable(tableModel);
		table.setRowHeight(20);
		table.setRowSorter(rowSorter);
		table.getTableHeader().setReorderingAllowed(false);

		scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		add(scrollPane, BorderLayout.CENTER);

		addListeners();
	}

	private void addListeners() {
		addListenerForFilterPopup();
		addFilterChangedListeners();

		ActionProviderListeners.addMouseListeners(table);
	}

	private void addListenerForFilterPopup() {
		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (!e.isPopupTrigger()) {
					return;
				}
				Point mousePositionInTable = e.getPoint();
				int col = table.columnAtPoint(mousePositionInTable);
				if (col < 0) {
					return;
				}
				showFilterPopup(col, e.getPoint());
			}
		});
	}

	private void addFilterChangedListeners() {
		int numColumns = tableModel.getColumnCount();
		for (int col = 0; col < numColumns; col++) {
			ValueFilter valueFilter = tableModel.getValueFilter(col);
			valueFilter.addFilterChangedListener(this::onFilterChanged);
		}
	}

	private void onFilterChanged() {
		rowSorter.sort();
		updateColumnNames();
	}

	private void updateColumnNames() {
		TableColumnModel columnModel = table.getColumnModel();
		int numColumns = tableModel.getColumnCount();
		for (int col = 0; col < numColumns; col++) {
			String columnName = tableModel.getColumnName(col);
			TableColumn column = columnModel.getColumn(col);
			column.setHeaderValue(columnName);
		}
		table.getTableHeader().repaint();
	}

	public JTable getInternalTable() {
		return table;
	}

	private void showFilterPopup(int column, Point mousePos) {
		ValueFilter valueFilter = tableModel.getValueFilter(column);
		if (valueFilter == ValueFilters.NONE) {
			return;
		}
		String columnName = tableModel.getPlainColumnName(column);
		JPanel filterPanel = new FilterPopupPanel(valueFilter, columnName);

		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(filterPanel);
		popupMenu.show(table.getTableHeader(), mousePos.x, mousePos.y);
	}
}
