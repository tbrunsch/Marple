package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.InspectionLink;
import com.AMS.jBEAM.objectInspection.InspectionUtils;
import com.AMS.jBEAM.objectInspection.swing.gui.fieldTable.SwingFieldTableColumnDescription;
import com.AMS.jBEAM.objectInspection.swing.gui.fieldTable.SwingFieldTableColumnDescriptionIF;
import com.AMS.jBEAM.objectInspection.swing.gui.fieldTable.SwingFieldTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SwingObjectInspectionPanel extends JPanel
{
    private static final Map<Integer, AccessModifier>   MODIFIER_TO_ACCESS_MODIFIER = new HashMap<>();

    static {
        MODIFIER_TO_ACCESS_MODIFIER.put(Modifier.PUBLIC,          AccessModifier.PUBLIC);
        MODIFIER_TO_ACCESS_MODIFIER.put(Modifier.PROTECTED,       AccessModifier.PROTECTED);
        MODIFIER_TO_ACCESS_MODIFIER.put(Modifier.PRIVATE,         AccessModifier.PRIVATE);
    }

    private final JScrollPane   scrollPane;
    private final JTable        table;

    public SwingObjectInspectionPanel(final Object object) {
        super(new GridBagLayout());

        List<Field> fields = InspectionUtils.getFields(object.getClass());
        fields.forEach(field -> field.setAccessible(true));
        List<SwingFieldTableColumnDescriptionIF> columnDescriptions = createColumnDescriptionsFor(object);
        SwingFieldTableModel fieldTableModel = new SwingFieldTableModel(fields, columnDescriptions);
        table = new JTable(fieldTableModel);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(AccessModifier.class, new AccessModifierRenderer());
        scrollPane = new JScrollPane(table);

        add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    private static List<SwingFieldTableColumnDescriptionIF> createColumnDescriptionsFor(final Object object) {
        return Arrays.asList(
            new SwingFieldTableColumnDescription("Name", String.class, Field::getName),
            new SwingFieldTableColumnDescription("Value", InspectionLink.class, field -> getInspectionLink(getFieldValue(field, object))),
            new SwingFieldTableColumnDescription("Type", Class.class, field -> field.getType().getSimpleName()),
            new SwingFieldTableColumnDescription("Class", String.class, field -> field.getDeclaringClass().getSimpleName()),
            new SwingFieldTableColumnDescription("Modifier", AccessModifier.class, field -> getAccessModifier(field))
        );
    }

    private static InspectionLink getInspectionLink(Object object) {
        return object == null ? null : new InspectionLink(object, object.toString());
    }

    private static Object getFieldValue(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            // Should not happen
            return null;
        }
    }

    private static AccessModifier getAccessModifier(Field field) {
        int modifiers = field.getModifiers();
        return getAccessModifier(modifiers);
    }

    private static AccessModifier getAccessModifier(int modifiers) {
        for (int modifier : MODIFIER_TO_ACCESS_MODIFIER.keySet()) {
            if ((modifiers & modifier) != 0) {
                return MODIFIER_TO_ACCESS_MODIFIER.get(modifier);
            }
        }
        return AccessModifier.PACKAGE_PRIVATE;
    }

    private enum AccessModifier
    {
        PUBLIC("public"),
        PROTECTED("protected"),
        PACKAGE_PRIVATE("package private"),
        PRIVATE("private");

        private final String name;

        AccessModifier(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class AccessModifierRenderer extends JLabel implements TableCellRenderer
    {
        private static final Map<AccessModifier, Color> MODIFIER_COLORS = new HashMap<>();

        static {
            MODIFIER_COLORS.put(AccessModifier.PUBLIC,          Color.GREEN.darker());
            MODIFIER_COLORS.put(AccessModifier.PROTECTED,       Color.BLUE.darker());
            MODIFIER_COLORS.put(AccessModifier.PACKAGE_PRIVATE, Color.MAGENTA.darker());
            MODIFIER_COLORS.put(AccessModifier.PRIVATE,         Color.RED.darker());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            Color color = MODIFIER_COLORS.get(value);
            setForeground(color == null ? Color.BLACK : color);
            setFont(getFont().deriveFont(0));
            return this;
        }
    }
}
