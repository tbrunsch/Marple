package dd.kms.marple.gui.table;

import dd.kms.marple.gui.filters.ValueFilter;

import java.util.function.Function;

class ColumnDescriptionImpl<T> implements ColumnDescription<T>
{
	private final String				name;
	private final Class<?>				clazz;
	private final Function<T, Object>	valueExtractor;
	private final ValueFilter valueFilter;
	private final EditorSettings<T>		editorSettings;

	ColumnDescriptionImpl(String name, Class<?> clazz, Function<T, Object> valueExtractor, ValueFilter valueFilter, EditorSettings<T> editorSettings) {
		this.name = name;
		this.clazz = clazz;
		this.valueExtractor = valueExtractor;
		this.valueFilter = valueFilter;
		this.editorSettings = editorSettings;
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
	public ValueFilter createValueFilter() {
		return valueFilter;
	}

	@Override
	public EditorSettings<T> getEditorSettings() {
		return editorSettings;
	}
}
