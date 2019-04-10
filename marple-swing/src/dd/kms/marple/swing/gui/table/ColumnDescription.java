package dd.kms.marple.swing.gui.table;

public interface ColumnDescription<T>
{
	String getName();
	Class<?> getColumnClass();
	Object extractValue(T element);
	TableValueFilter createValueFilter();
}
