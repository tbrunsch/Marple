package dd.kms.marple.impl.gui.inspector.views.methodview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.gui.filters.ValueFilters;
import dd.kms.marple.impl.gui.table.*;
import dd.kms.zenodot.api.common.MethodScanner;
import dd.kms.zenodot.api.common.MethodScannerBuilder;
import dd.kms.zenodot.api.common.StaticMode;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;

class MethodTable extends JPanel implements ObjectView
{
	private final ListBasedTable<Method>	table;

	private final InspectionContext			context;

	private final MethodViewUtils			methodViewUtils;

	public MethodTable(Object object, InspectionContext context) {
		super(new GridBagLayout());

		this.context = context;

		this.methodViewUtils = new MethodViewUtils(object, context);

		MethodScanner methodScanner = MethodScannerBuilder.create().staticMode(StaticMode.BOTH).build();
		List<Method> methods = methodScanner.getMethods(object.getClass());
		List<ColumnDescription<Method>> columnDescriptions = createColumnDescriptions();

		table = new ListBasedTable<>(methods, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new ActionProviderRenderer());
		internalTable.setDefaultRenderer(Member.class, new MemberRenderer());
		internalTable.setDefaultRenderer(Class.class, new ClassRenderer(context));

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));
	}

	@Override
	public String getViewName() {
		return "Detailed Method View";
	}

	@Override
	public Component getViewComponent() {
		return this;
	}

	@Override
	public Object getViewSettings() {
		return table.getViewSettings();
	}

	@Override
	public void applyViewSettings(Object settings, ViewSettingsOrigin origin) {
		table.applyViewSettings(settings, origin);
	}

	private List<ColumnDescription<Method>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<Method>("Return Type",	Class.class,	method -> method.getReturnType()							).valueFilter(ValueFilters.createWildcardFilter(o -> context.getDisplayText(o))).build(),
			new ColumnDescriptionBuilder<Method>("Name",		Object.class,	method -> methodViewUtils.getMethodActionProvider(method)	).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Method>("Arguments",	String.class,	method -> MethodViewUtils.formatArguments(method, context)	).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<Method>("Class",		Class.class, 	method -> method.getDeclaringClass()						).valueFilter(ValueFilters.createSelectionFilter(context)).build(),
			new ColumnDescriptionBuilder<Method>("Modifier",	Member.class,	method -> method											).valueFilter(ValueFilters.createModifierFilter(false)).build()
		);
	}
}
