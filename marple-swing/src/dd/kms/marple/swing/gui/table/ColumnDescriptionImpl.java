package dd.kms.marple.swing.gui.table;

import java.util.function.Function;

class ColumnDescriptionImpl<T> implements ColumnDescription<T>
{
	private final String				name;
	private final Class<?>				clazz;
	private final Function<T, Object>	valueExtractor;
	private final TableValueFilter valueFilter;

	ColumnDescriptionImpl(String name, Class<?> clazz, Function<T, Object> valueExtractor, TableValueFilter valueFilter) {
		this.name = name;
		this.clazz = clazz;
		this.valueExtractor = valueExtractor;
		this.valueFilter = valueFilter;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getColumnClass() {
		return clazz;
	}

	@Override
	public Object extractValue(T element) {
		return valueExtractor.apply(element);
	}

	@Override
	public TableValueFilter createValueFilter() {
		return valueFilter;
	}
}
