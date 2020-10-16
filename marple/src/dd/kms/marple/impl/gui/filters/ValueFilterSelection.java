package dd.kms.marple.impl.gui.filters;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

class ValueFilterSelection extends AbstractValueFilter
{
	private final JPanel					editor				= new JPanel();

	private final InspectionContext			context;

	// Map to true if it should be shown
	private final Map<Object, JCheckBox>	checkBoxesByValue	= new LinkedHashMap<>();

	ValueFilterSelection(InspectionContext context) {
		this.context = context;

		editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS));
		editor.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	@Override
	public boolean isActive() {
		return checkBoxesByValue.values().stream().anyMatch(checkBox -> !checkBox.isSelected());
	}

	@Override
	public void addAvailableValue(Object o) {
		registerValue(o);
	}

	@Override
	public Component getEditor() {
		return editor;
	}

	@Override
	public Object getSettings() {
		return new ValueFilterSelectionSettings(getFilteredValues());
	}

	@Override
	public void applySettings(Object settings) {
		if (settings instanceof ValueFilterSelectionSettings) {
			ValueFilterSelectionSettings filterSettings = (ValueFilterSelectionSettings) settings;
			Map<Object, Boolean> filteredValues = filterSettings.getFilteredValues();
			for (Map.Entry<Object, Boolean> entry : filteredValues.entrySet()) {
				Object value = entry.getKey();
				JCheckBox checkBox = checkBoxesByValue.get(value);
				if (checkBox != null) {
					checkBox.setSelected(entry.getValue());
				}
			}
		}
	}

	@Override
	public boolean test(Object o) {
		JCheckBox checkBox = checkBoxesByValue.get(o);
		return checkBox != null && checkBox.isSelected();
	}

	private void registerValue(Object value) {
		if (checkBoxesByValue.containsKey(value)) {
			return;
		}
		JCheckBox checkBox = new JCheckBox(getDisplayText(value));
		checkBoxesByValue.put(value, checkBox);
		checkBox.setSelected(true);
		editor.add(checkBox);
		checkBox.addItemListener(e -> fireFilterChanged());
		Dimension maxCheckBoxDimension = getMaximumCheckBoxDimension();
		setPreferredCheckBoxSizes(maxCheckBoxDimension);
		fireFilterChanged();
	}

	private Dimension getMaximumCheckBoxDimension() {
		int maxWidth = 0;
		int maxHeight = 0;
		for (JCheckBox checkBox : checkBoxesByValue.values()) {
			Dimension preferredSize = checkBox.getPreferredSize();
			maxWidth = Math.max(maxWidth, preferredSize.width);
			maxHeight = Math.max(maxHeight, preferredSize.height);
		}
		return new Dimension(maxWidth, maxHeight);
	}

	private void setPreferredCheckBoxSizes(Dimension size) {
		for (JCheckBox checkBox : checkBoxesByValue.values()) {
			checkBox.setPreferredSize(size);
		}
	}

	private String getDisplayText(Object value) {
		return value instanceof ObjectInfo || value instanceof TypeInfo
				? value.toString()
				: context.getDisplayText(InfoProvider.createObjectInfo(value));
	}

	private Map<Object, Boolean> getFilteredValues() {
		return checkBoxesByValue.entrySet().stream().collect(Collectors.toMap(
				entry -> entry.getKey(),
				entry -> entry.getValue().isSelected()
			));
	}

	private static class ValueFilterSelectionSettings
	{
		private final Map<Object, Boolean>	filteredValues;

		ValueFilterSelectionSettings(Map<Object, Boolean> filteredValues) {
			this.filteredValues = filteredValues;
		}

		Map<Object, Boolean> getFilteredValues() {
			return filteredValues;
		}
	}
}
