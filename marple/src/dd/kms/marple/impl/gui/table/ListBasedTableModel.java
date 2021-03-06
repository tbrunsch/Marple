package dd.kms.marple.impl.gui.table;

import dd.kms.marple.impl.gui.filters.ValueFilter;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ListBasedTableModel<T> extends AbstractTableModel
{
	private final List<T>					 	list;
	private final List<ColumnDescription<T>>	columnDescriptions;
	private final List<ValueFilter>				valueFilters;

	public ListBasedTableModel(List<T> list, List<ColumnDescription<T>> columnDescriptions) {
		this.list = list;
		this.columnDescriptions = columnDescriptions;
		this.valueFilters = createValueFilters();
	}

	private List<ValueFilter> createValueFilters() {
		int numRows = getRowCount();
		int numCols = getColumnCount();
		List<ValueFilter> valueFilters = new ArrayList<>(numCols);
		for (int col = 0; col < numCols; col++) {
			ColumnDescription<T> columnDescription = columnDescriptions.get(col);
			ValueFilter valueFilter = columnDescription.getValueFilter();
			if (valueFilter != null) {
				for (int row = 0; row < numRows; row++) {
					Object value = getValueAt(row, col);
					valueFilter.addAvailableValue(value);
				}
			}
			valueFilters.add(valueFilter);
		}
		return valueFilters;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public int getColumnCount() {
		return columnDescriptions.size();
	}

	@Override
	public String getColumnName(int col) {
		ValueFilter valueFilter = getValueFilter(col);
		String prefix = valueFilter.isActive() ? "*" : "";
		return prefix + getPlainColumnName(col);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return columnDescriptions.get(col).getColumnClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		EditorSettings<T> editorSettings = columnDescriptions.get(col).getEditorSettings();
		return editorSettings != null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return columnDescriptions.get(col).extractValue(list.get(row));
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		EditorSettings<T> editorSettings = columnDescriptions.get(col).getEditorSettings();
		if (editorSettings == null) {
			throw new IllegalStateException("Cell is not editable");
		}
		editorSettings.setElementValue(list, row, value);
		fireTableChanged(new TableModelEvent(this));
	}

	String getPlainColumnName(int col) {
		return columnDescriptions.get(col).getName();
	}

	public ValueFilter getValueFilter(int col) {
		return valueFilters.get(col);
	}
}
