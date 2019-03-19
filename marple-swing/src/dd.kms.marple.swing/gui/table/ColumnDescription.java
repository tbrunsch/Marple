package dd.kms.marple.swing.gui.table;

import java.lang.reflect.Field;
import java.util.function.Function;

public class ColumnDescription<T> implements ColumnDescriptionIF<T>
{
	private final String				name;
	private final Class<?>				clazz;
	private final Function<T, Object>	valueExtractor;
	private final TableValueFilterIF	valueFilter;

	public ColumnDescription(String name, Class<?> clazz, Function<T, Object> valueExtractor, TableValueFilterIF valueFilter) {
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
	public TableValueFilterIF createValueFilter() {
		return valueFilter;
	}
}
