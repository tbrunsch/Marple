package com.AMS.jBEAM.objectInspection.swing.gui.table;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ListBasedTableModel<T> extends AbstractTableModel
{
	private final List<T>					 	list;
	private final List<ColumnDescriptionIF<T>>	columnDescriptions;
	private final List<TableValueFilterIF>		valueFilters;

	public ListBasedTableModel(List<T> list, List<ColumnDescriptionIF<T>> columnDescriptions) {
		this.list = list;
		this.columnDescriptions = columnDescriptions;
		this.valueFilters = createValueFilters();
	}

	private List<TableValueFilterIF> createValueFilters() {
		int numRows = getRowCount();
		int numCols = getColumnCount();
		List<TableValueFilterIF> valueFilters = new ArrayList<>(numCols);
		for (int col = 0; col < numCols; col++) {
			ColumnDescriptionIF<T> columnDescription = columnDescriptions.get(col);
			TableValueFilterIF valueFilter = columnDescription.createValueFilter();
			for (int row = 0; row < numRows; row++) {
				Object value = getValueAt(row, col);
				valueFilter.addAvailableValue(value);
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
		TableValueFilterIF valueFilter = getValueFilter(col);
		String prefix = valueFilter.isActive() ? "*" : "";
		return prefix + getPlainColumnName(col);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return columnDescriptions.get(col).getColumnClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return columnDescriptions.get(col).extractValue(list.get(row));
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int col) {
		throw new IllegalStateException("Cell is not editable");
	}

	String getPlainColumnName(int col) {
		return columnDescriptions.get(col).getName();
	}

	TableValueFilterIF getValueFilter(int col) {
		return valueFilters.get(col);
	}
}
