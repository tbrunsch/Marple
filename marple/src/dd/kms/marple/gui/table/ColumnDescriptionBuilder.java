package dd.kms.marple.gui.table;

import dd.kms.marple.gui.filters.ValueFilter;
import dd.kms.marple.gui.filters.ValueFilters;

import java.util.function.Function;

public class ColumnDescriptionBuilder<T>
{
	private final String				name;
	private final Class<?>				clazz;
	private final Function<T, Object>	valueExtractor;

	private ValueFilter valueFilter		= ValueFilters.NONE;
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

	public ColumnDescriptionBuilder<T> editorSettings(ElementGenerator<T> elementGenerator) {
		this.editorSettings = new EditorSettingsImpl<T>(elementGenerator);
		return this;
	}

	public ColumnDescription build() {
		return new ColumnDescriptionImpl<>(name, clazz, valueExtractor, valueFilter, editorSettings);
	}

	@FunctionalInterface
	public interface ElementGenerator<T> {
		T deriveFrom(T old, Object value);
	}
}
