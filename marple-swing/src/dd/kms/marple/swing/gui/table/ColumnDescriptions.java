package dd.kms.marple.swing.gui.table;

import java.util.function.Function;

public class ColumnDescriptions
{
	public static <T> ColumnDescription<T> of(String name, Class<?> clazz, Function<T, Object> valueExtractor, TableValueFilter valueFilter) {
		return new ColumnDescriptionImpl<>(name, clazz, valueExtractor, valueFilter);
	}
}
