package dd.kms.marple.impl.gui.inspector.views.methodview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.gui.filters.ValueFilters;
import dd.kms.marple.impl.gui.table.*;
import dd.kms.zenodot.api.common.MethodScanner;
import dd.kms.zenodot.api.common.MethodScannerBuilder;
import dd.kms.zenodot.api.common.StaticMode;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.MemberInfo;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;

class MethodTable extends JPanel
{
	private final ListBasedTable<ExecutableInfo>	table;

	private final InspectionContext					context;

	private final MethodViewUtils					methodViewUtils;

	public MethodTable(ObjectInfo objectInfo, InspectionContext context) {
		super(new GridBagLayout());

		this.context = context;

		this.methodViewUtils = new MethodViewUtils(objectInfo, context);

		MethodScanner methodScanner = MethodScannerBuilder.create().staticMode(StaticMode.BOTH).build();
		List<ExecutableInfo> methodInfos = InfoProvider.getMethodInfos(ReflectionUtils.getRuntimeTypeInfo(objectInfo), methodScanner);
		List<ColumnDescription<ExecutableInfo>> columnDescriptions = createColumnDescriptions();

		table = new ListBasedTable<>(methodInfos, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new ActionProviderRenderer());
		internalTable.setDefaultRenderer(MemberInfo.class, new MemberInfoRenderer());

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));
	}

	private List<ColumnDescription<ExecutableInfo>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<ExecutableInfo>("Return Type",	String.class, 		methodInfo -> methodInfo.getReturnType().toString()					).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<ExecutableInfo>("Name",		Object.class, 		methodInfo -> methodViewUtils.getMethodActionProvider(methodInfo)	).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<ExecutableInfo>("Arguments",	String.class, 		methodInfo -> methodInfo.formatArguments()							).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<ExecutableInfo>("Class",		String.class, 		methodInfo -> methodInfo.getDeclaringType().toString()				).valueFilter(ValueFilters.createSelectionFilter(context)).build(),
			new ColumnDescriptionBuilder<ExecutableInfo>("Modifier",	MemberInfo.class,	methodInfo -> methodInfo											).valueFilter(ValueFilters.createModifierFilter(false)).build()
		);
	}
}
