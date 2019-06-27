package dd.kms.marple.gui.inspector.views.methodview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.filters.ValueFilters;
import dd.kms.marple.gui.table.*;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.MethodScanner;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

class MethodTable extends JPanel
{
	private final ListBasedTable<Method>	table;

	private final InspectionContext			inspectionContext;

	private final MethodViewUtils			methodViewUtils;

	public MethodTable(Object object, InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;

		this.methodViewUtils = new MethodViewUtils(object, inspectionContext);

		List<Method> methods = new MethodScanner().getMethods(object.getClass());
		List<ColumnDescription<Method>> columnDescriptions = createColumnDescriptions();

		table = new ListBasedTable<>(methods, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new ActionProviderRenderer());
		internalTable.setDefaultRenderer(AccessModifier.class, new AccessModifierRenderer());

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	private List<ColumnDescription<Method>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<Method>("Return Type",	String.class, 			method -> method.getReturnType().getSimpleName()			).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Method>("Name",		Object.class, 			method -> methodViewUtils.getMethodActionProvider(method)	).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Method>("Arguments",	String.class, 			method -> methodViewUtils.getArgumentsAsString(method)		).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Method>("Class",		String.class, 			method -> method.getDeclaringClass().getSimpleName()		).valueFilter(ValueFilters.createSelectionFilter(inspectionContext)).build(),
			new ColumnDescriptionBuilder<Method>("Modifier",	AccessModifier.class,	method -> methodViewUtils.getAccessModifier(method)			).valueFilter(ValueFilters.createMinimumAccessLevelFilter()).build()
		);
	}
}
