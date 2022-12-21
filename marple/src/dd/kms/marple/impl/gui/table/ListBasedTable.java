package dd.kms.marple.impl.gui.table;

import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.impl.gui.filters.ValueFilter;
import dd.kms.marple.impl.gui.filters.ValueFilters;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A table whose rows values only depend on a 1-dimensional list.
 * Which value is displayed in which row is defined by the
 * corresponding {@link ColumnDescription}.
 *
 * @param <T>
 */
public class ListBasedTable<T> extends JPanel implements ObjectView, Disposable
{
	private final List<T>					list;
	private final ListBasedTableModel<T>	tableModel;
	private final TableRowSorter			rowSorter;
	private final JTable					table;
	private final JScrollPane				scrollPane;

	public ListBasedTable(List<T> list, List<ColumnDescription<T>> columnDescriptions) {
		super(new BorderLayout());

		this.list = list;

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

	public JTable getInternalTable() {
		return table;
	}

	@Override
	public String getViewName() {
		return "Table";
	}

	@Override
	public Component getViewComponent() {
		return this;
	}

	@Override
	public Object getViewSettings() {
		int numColumns = tableModel.getColumnCount();
		List<Object> filterSettings = IntStream.range(0, numColumns)
			.mapToObj(tableModel::getValueFilter)
			.map(ValueFilter::getSettings)
			.collect(Collectors.toList());
		return new ListBasedTableSettings(filterSettings);
	}

	@Override
	public void applyViewSettings(Object settings, ViewSettingsOrigin origin) {
		if (settings instanceof ListBasedTableSettings) {
			ListBasedTableSettings tableSettings = (ListBasedTableSettings) settings;
			if (origin == ViewSettingsOrigin.SAME_CONTEXT) {
				List<Object> filterSettings = tableSettings.getFilterSettings();
				int numColumns = tableModel.getColumnCount();
				assert numColumns == filterSettings.size() : "Table settings do not match the current columns";
				for (int col = 0; col < numColumns; col++) {
					ValueFilter valueFilter = tableModel.getValueFilter(col);
					Object valueFilterSettings = filterSettings.get(col);
					valueFilter.applySettings(valueFilterSettings);
				}
			}
		}
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

	@Override
	public void dispose() {
		list.clear();
	}

	private static class ListBasedTableSettings
	{
		private final List<Object>	filterSettings;

		ListBasedTableSettings(List<Object> filterSettings) {
			this.filterSettings = filterSettings;
		}

		List<Object> getFilterSettings() {
			return filterSettings;
		}
	}
}
