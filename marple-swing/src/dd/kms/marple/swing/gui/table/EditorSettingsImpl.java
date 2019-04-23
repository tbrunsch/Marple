package dd.kms.marple.swing.gui.table;

import dd.kms.marple.swing.gui.table.ColumnDescriptionBuilder.ElementGenerator;

import java.util.List;

class EditorSettingsImpl<T> implements EditorSettings<T>
{
	private final ElementGenerator<T>	elementGenerator;

	EditorSettingsImpl(ElementGenerator<T> elementGenerator) {
		this.elementGenerator = elementGenerator;
	}

	@Override
	public void setElementValue(List<T> list, int elementIndex, Object value) {
		T oldElement = list.get(elementIndex);
		T newElement = elementGenerator.deriveFrom(oldElement, value);
		list.set(elementIndex, newElement);
	}
}
