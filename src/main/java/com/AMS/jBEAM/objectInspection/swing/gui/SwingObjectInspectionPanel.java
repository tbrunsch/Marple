package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.InspectionLink;
import com.AMS.jBEAM.objectInspection.InspectionUtils;
import com.AMS.jBEAM.objectInspection.swing.gui.table.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.GridBagConstraints.*;

class SwingObjectInspectionPanel extends JPanel
{
    private static final Map<Integer, AccessModifier>   MODIFIER_TO_ACCESS_MODIFIER = new HashMap<>();

    static {
        MODIFIER_TO_ACCESS_MODIFIER.put(Modifier.PUBLIC,          AccessModifier.PUBLIC);
        MODIFIER_TO_ACCESS_MODIFIER.put(Modifier.PROTECTED,       AccessModifier.PROTECTED);
        MODIFIER_TO_ACCESS_MODIFIER.put(Modifier.PRIVATE,         AccessModifier.PRIVATE);
    }

    private final ListBasedTable<Field> table;

    public SwingObjectInspectionPanel(final Object object) {
        super(new GridBagLayout());

        List<Field> fields = InspectionUtils.getFields(object.getClass());
        fields.forEach(field -> field.setAccessible(true));

        List<ColumnDescriptionIF<Field>> columnDescriptions = createColumnDescriptionsFor(object);

        table = new ListBasedTable<>(fields, columnDescriptions);
        table.getInternalTable().setDefaultRenderer(AccessModifier.class, new AccessModifierRenderer());

        add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    private static List<ColumnDescriptionIF<Field>> createColumnDescriptionsFor(final Object object) {
        return Arrays.asList(
            new ColumnDescription<>("Name",     String.class,           Field::getName,                                             new TableValueFilter_Wildcard()),
            new ColumnDescription<>("Value",    Object.class,           field -> getInspectionLink(field, object),                  new TableValueFilter_Wildcard()),
            new ColumnDescription<>("Type",     Class.class,            field -> field.getType().getSimpleName(),                   new TableValueFilter_Wildcard()),
            new ColumnDescription<>("Class",    String.class,           field -> field.getDeclaringClass().getSimpleName(),         new TableValueFilter_Selection()),
            new ColumnDescription<>("Modifier", AccessModifier.class,   field -> getAccessModifier(field),                          new TableValueFilter_Selection())
        );
    }

    private static Object getInspectionLink(Field field, Object object) {
        Object fieldValue = getFieldValue(field, object);
        if (fieldValue == null) {
            return null;
        }
        return field.getType().isPrimitive()
                ? fieldValue.toString()
                : new InspectionLink(fieldValue, fieldValue.toString());
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
