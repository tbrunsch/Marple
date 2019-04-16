package dd.kms.marple.swing.gui.table;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class TableValueFilterSelection extends AbstractTableValueFilter
{
	private final InspectionContext<?, ?>	inspectionContext;

	// Map to true if it should be shown
	private final Map<Object, Boolean>		filteredValues = new LinkedHashMap<>();

	TableValueFilterSelection(InspectionContext<?, ?> inspectionContext) {
		this.inspectionContext = inspectionContext;
	}

	@Override
	public boolean isActive() {
		return filteredValues.values().contains(false);
	}

	@Override
	public void addAvailableValue(Object o) {
		setAllowed(o, true);
	}

	@Override
	public Component getEditor() {
		List<JCheckBox> checkBoxes = filteredValues.keySet().stream().map(value -> createCheckbox(value, filteredValues.get(value))).collect(Collectors.toList());
		int maxWidth = 0;
		int maxHeight = 0;
		for (JCheckBox checkBox : checkBoxes) {
			Dimension preferredSize = checkBox.getPreferredSize();
			maxWidth = Math.max(maxWidth, preferredSize.width);
			maxHeight = Math.max(maxHeight, preferredSize.height);
		}
		Dimension maxDimension = new Dimension(maxWidth, maxHeight);

		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
		filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		for (JCheckBox checkBox : checkBoxes) {
			checkBox.setPreferredSize(maxDimension);
			filterPanel.add(checkBox);
		}

		return filterPanel;
	}

	@Override
	public boolean test(Object o) {
		Boolean allowed = filteredValues.get(o);
		return allowed != null && allowed == true;
	}

	private void setAllowed(Object value, boolean allowed) {
		filteredValues.put(value, allowed);
		fireFilterChanged();
	}

	private JCheckBox createCheckbox(Object value, boolean allowed) {
		JCheckBox checkBox = new JCheckBox(inspectionContext.getDisplayText(value));
		checkBox.setSelected(allowed);
		checkBox.addItemListener(e -> setAllowed(value, e.getStateChange() == ItemEvent.SELECTED));
		return checkBox;
	}
}
