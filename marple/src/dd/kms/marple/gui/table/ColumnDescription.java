package dd.kms.marple.gui.table;

public interface ColumnDescription<T>
{
	String getName();
	Class<?> getColumnClass();
	Object extractValue(T element);
	TableValueFilter createValueFilter();
	EditorSettings<T> getEditorSettings();
}
