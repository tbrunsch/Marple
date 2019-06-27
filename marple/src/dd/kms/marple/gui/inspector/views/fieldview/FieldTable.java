package dd.kms.marple.gui.inspector.views.fieldview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.gui.filters.ValueFilters;
import dd.kms.marple.gui.table.*;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.FieldScanner;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldTable extends JPanel
{
	private final ListBasedTable<Field>	table;

	private final Object				object;
	private final InspectionContext		inspectionContext;

	public FieldTable(Object object, InspectionContext inspectionContext) {
		super(new GridBagLayout());
		this.object = object;
		this.inspectionContext = inspectionContext;

		List<Field> fields = new FieldScanner().getFields(object.getClass(), false);
		fields.forEach(field -> field.setAccessible(true));

		List<ColumnDescription<Field>> columnDescriptions = createColumnDescriptions();

		table = new ListBasedTable<>(fields, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new ActionProviderRenderer());
		internalTable.setDefaultRenderer(AccessModifier.class, new AccessModifierRenderer());

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	private List<ColumnDescription<Field>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<Field>("Name",		String.class,			field -> field.getName())							.valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Field>("Value",	ActionProvider.class, 	field -> getFieldValueActionProvider(field))		.valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Field>("Type",		Class.class,			field -> field.getType().getSimpleName())			.valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Field>("Class",	String.class,			field -> field.getDeclaringClass().getSimpleName())	.valueFilter(ValueFilters.createSelectionFilter(inspectionContext)).build(),
			new ColumnDescriptionBuilder<Field>("Modifier",	AccessModifier.class,	field -> getAccessModifier(field))					.valueFilter(ValueFilters.createMinimumAccessLevelFilter()).build()
		);
	}

	private ActionProvider getFieldValueActionProvider(Field field) {
		String fieldName = field.getName();
		Object fieldValue = getFieldValue(field);
		return new ActionProviderBuilder(inspectionContext.getDisplayText(fieldValue), fieldValue, inspectionContext)
				.evaluateAs(fieldName, object)
				.build();
	}

	private Object getFieldValue(Field field) {
		try {
			return field.get(object);
		} catch (IllegalAccessException e) {
			// should not happen
			return null;
		}
	}

	private static AccessModifier getAccessModifier(Field field) {
		int modifiers = field.getModifiers();
		return AccessModifier.getValue(modifiers);
	}
}
