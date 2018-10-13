package com.AMS.jBEAM.objectInspection.swing.gui.table;

import com.AMS.jBEAM.objectInspection.common.DisplayUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableValueFilter_Selection extends AbstractTableValueFilter
{
    // Map to true if it should be shown
    private final Map<Object, Boolean> filteredValues = new LinkedHashMap<>();

    @Override
    public boolean isActive() {
        return filteredValues.values().contains(false);
    }

    @Override
    public void addAvailableValue(Object o) {
        setAllowed(o, true);
    }

    @Override
    public JComponent getEditor() {
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

    private JCheckBox createCheckbox(final Object value, boolean allowed) {
        JCheckBox checkBox = new JCheckBox(DisplayUtils.toString(value));
        checkBox.setSelected(allowed);
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean allowed = e.getStateChange() == ItemEvent.SELECTED;
                setAllowed(value, allowed);
            }
        });
        return checkBox;
    }
}
