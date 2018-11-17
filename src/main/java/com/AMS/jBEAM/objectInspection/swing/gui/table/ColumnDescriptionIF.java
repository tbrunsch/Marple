package com.AMS.jBEAM.objectInspection.swing.gui.table;

public interface ColumnDescriptionIF<T>
{
	String getName();
	Class<?> getColumnClass();
	Object extractValue(T element);
	TableValueFilterIF createValueFilter();
}
