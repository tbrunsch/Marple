package com.AMS.jBEAM.objectInspection.swing.gui.fieldTable;

import com.AMS.jBEAM.objectInspection.InspectionLink;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Field;
import java.util.List;

public class SwingFieldTableModel extends AbstractTableModel
{
    private final List<Field>                               fields;
    private final List<SwingFieldTableColumnDescriptionIF>  columnDescriptions;

    public SwingFieldTableModel(List<Field> fields, List<SwingFieldTableColumnDescriptionIF> columnDescriptions) {
        this.fields = fields;
        this.columnDescriptions = columnDescriptions;
    }

    @Override
    public int getRowCount() {
        return fields.size();
    }

    @Override
    public int getColumnCount() {
        return columnDescriptions.size();
    }

    @Override
    public String getColumnName(int colIndex) {
        return columnDescriptions.get(colIndex).getName();
    }

    @Override
    public Class<?> getColumnClass(int colIndex) {
        return columnDescriptions.get(colIndex).getColumnClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int colIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int colIndex) {
        return columnDescriptions.get(colIndex).extractValue(fields.get(rowIndex));
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int colIndex) {
        throw new IllegalStateException("Cell is not editable");
    }
}
