package dd.kms.marple.impl.gui.table;

import dd.kms.marple.impl.gui.filters.ValueFilter;

public interface ColumnDescription<T>
{
	String getName();
	Class<?> getColumnClass();
	Object extractValue(T element);
	ValueFilter createValueFilter();
	EditorSettings<T> getEditorSettings();
}
