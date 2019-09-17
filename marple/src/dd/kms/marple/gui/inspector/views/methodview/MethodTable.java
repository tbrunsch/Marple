package dd.kms.marple.gui.inspector.views.methodview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.gui.filters.ValueFilters;
import dd.kms.marple.gui.table.*;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.MethodScanner;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;

class MethodTable extends JPanel
{
	private final ListBasedTable<ExecutableInfo>	table;

	private final InspectionContext					inspectionContext;

	private final MethodViewUtils					methodViewUtils;

	public MethodTable(ObjectInfo objectInfo, InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;

		this.methodViewUtils = new MethodViewUtils(objectInfo, inspectionContext);

		List<ExecutableInfo> methodInfos = InfoProvider.getMethodInfos(ReflectionUtils.getRuntimeTypeInfo(objectInfo), new MethodScanner());
		List<ColumnDescription<ExecutableInfo>> columnDescriptions = createColumnDescriptions();

		table = new ListBasedTable<>(methodInfos, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new ActionProviderRenderer());
		internalTable.setDefaultRenderer(AccessModifier.class, new AccessModifierRenderer());

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));
	}

	private List<ColumnDescription<ExecutableInfo>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<ExecutableInfo>("Return Type",	String.class, 			methodInfo -> methodInfo.getReturnType().toString()					).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<ExecutableInfo>("Name",		Object.class, 			methodInfo -> methodViewUtils.getMethodActionProvider(methodInfo)	).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<ExecutableInfo>("Arguments",	String.class, 			methodInfo -> methodInfo.formatArguments()							).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<ExecutableInfo>("Class",		String.class, 			methodInfo -> methodInfo.getDeclaringType().toString()				).valueFilter(ValueFilters.createSelectionFilter(inspectionContext)).build(),
			new ColumnDescriptionBuilder<ExecutableInfo>("Modifier",	AccessModifier.class,	methodInfo -> methodInfo.getAccessModifier()						).valueFilter(ValueFilters.createMinimumAccessLevelFilter()).build()
		);
	}
}
