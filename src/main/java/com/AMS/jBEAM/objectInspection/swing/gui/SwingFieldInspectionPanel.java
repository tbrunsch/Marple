package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.objectInspection.common.AccessModifier;
import com.AMS.jBEAM.objectInspection.swing.SwingObjectInspector;
import com.AMS.jBEAM.objectInspection.swing.gui.table.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

class SwingFieldInspectionPanel extends JPanel
{
	private final ListBasedTable<Field> table;

	public SwingFieldInspectionPanel(final Object object) {
		super(new GridBagLayout());

		List<Field> fields = ReflectionUtils.getFields(object.getClass(), false);
		fields.forEach(field -> field.setAccessible(true));

		List<ColumnDescriptionIF<Field>> columnDescriptions = createColumnDescriptionsFor(object);

		table = new ListBasedTable<>(fields, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new RunnableRenderer());
		internalTable.setDefaultRenderer(AccessModifier.class, new AccessModifierRenderer());

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	private static List<ColumnDescriptionIF<Field>> createColumnDescriptionsFor(final Object object) {
		return Arrays.asList(
			new ColumnDescription<>("Name",		String.class,			Field::getName,										new TableValueFilter_Wildcard()),
			new ColumnDescription<>("Value",		Object.class,			field -> getFieldValueLink(field, object),			new TableValueFilter_Wildcard()),
			new ColumnDescription<>("Type",		Class.class,			field -> field.getType().getSimpleName(),			new TableValueFilter_Wildcard()),
			new ColumnDescription<>("Class",		String.class,			field -> field.getDeclaringClass().getSimpleName(),	new TableValueFilter_Selection()),
			new ColumnDescription<>("Modifier",	AccessModifier.class,	field -> getAccessModifier(field),					new TableValueFilter_Selection())
		);
	}

	private static Object getFieldValueLink(Field field, Object object) {
		Object fieldValue = getFieldValue(field, object);
		if (fieldValue == null) {
			return null;
		}
		return field.getType().isPrimitive()
				? fieldValue.toString()
				: SwingObjectInspector.getInspector().createObjectInspectionLink(fieldValue);
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
		return AccessModifier.getValue(modifiers);
	}
}
