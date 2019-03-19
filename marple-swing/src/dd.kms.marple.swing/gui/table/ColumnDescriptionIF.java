package dd.kms.marple.swing.gui.table;

public interface ColumnDescriptionIF<T>
{
	String getName();
	Class<?> getColumnClass();
	Object extractValue(T element);
	TableValueFilterIF createValueFilter();
}
