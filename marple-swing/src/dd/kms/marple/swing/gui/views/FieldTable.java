package dd.kms.marple.swing.gui.views;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.common.AccessModifier;
import dd.kms.marple.swing.gui.table.*;
import dd.kms.zenodot.common.ReflectionUtils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldTable extends JPanel
{
	private final ListBasedTable<Field>			table;

	private final Object						object;
	private final InspectionContext<Component>	inspectionContext;

	public FieldTable(Object object, InspectionContext<Component> inspectionContext) {
		super(new GridBagLayout());
		this.object = object;
		this.inspectionContext = inspectionContext;

		List<Field> fields = ReflectionUtils.getFields(object.getClass(), false);
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
			ColumnDescriptions.of("Name",		String.class,			Field::getName,										TableValueFilters.createWildcardFilter()),
			ColumnDescriptions.of("Value",		ActionProvider.class,	field -> getFieldValueActionProvider(field),		TableValueFilters.createWildcardFilter()),
			ColumnDescriptions.of("Type",		Class.class,			field -> field.getType().getSimpleName(),			TableValueFilters.createWildcardFilter()),
			ColumnDescriptions.of("Class",		String.class,			field -> field.getDeclaringClass().getSimpleName(),	TableValueFilters.createSelectionFilter(inspectionContext)),
			ColumnDescriptions.of("Modifier",	AccessModifier.class,	field -> getAccessModifier(field),					TableValueFilters.createSelectionFilter(inspectionContext))
		);
	}

	private ActionProvider getFieldValueActionProvider(Field field) {
		Object fieldValue = getFieldValue(field);
		if (fieldValue == null) {
			return null;
		}
		ImmutableList.Builder<InspectionAction> actionsBuilder = ImmutableList.builder();
		boolean primitiveValue = field.getType().isPrimitive();
		if (!primitiveValue) {
			actionsBuilder.add(inspectionContext.createInspectObjectAction(fieldValue));
			if (fieldValue instanceof Component) {
				actionsBuilder.add(inspectionContext.createHighlightComponentAction((Component) fieldValue));
			}
			actionsBuilder.add(inspectionContext.createEvaluateAsThisAction(fieldValue));
		}
		actionsBuilder.add(inspectionContext.createEvaluateExpressionAction(field.getName(), object));
		return ActionProvider.of(inspectionContext.getDisplayText(fieldValue), actionsBuilder.build());
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
