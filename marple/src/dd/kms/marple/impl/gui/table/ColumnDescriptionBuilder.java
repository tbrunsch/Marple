package dd.kms.marple.impl.gui.table;

import dd.kms.marple.impl.gui.filters.ValueFilter;
import dd.kms.marple.impl.gui.filters.ValueFilters;

import java.util.function.Function;

public class ColumnDescriptionBuilder<T>
{
	private final String				name;
	private final Class<?>				clazz;
	private final Function<T, Object>	valueExtractor;

	private ValueFilter					valueFilter		= ValueFilters.NONE;
	private EditorSettings<T>			editorSettings	= null;

	public ColumnDescriptionBuilder(String name, Class<?> clazz, Function<T, Object> valueExtractor) {
		this.name = name;
		this.clazz = clazz;
		this.valueExtractor = valueExtractor;
	}

	public ColumnDescriptionBuilder<T> valueFilter(ValueFilter valueFilter) {
		this.valueFilter = valueFilter;
		return this;
	}

	public ColumnDescriptionBuilder<T> editorSettings(EditorSettings<T> editorSettings) {
		this.editorSettings = editorSettings;
		return this;
	}

	public ColumnDescription<T> build() {
		return new ColumnDescriptionImpl<>(name, clazz, valueExtractor, valueFilter, editorSettings);
	}
}
