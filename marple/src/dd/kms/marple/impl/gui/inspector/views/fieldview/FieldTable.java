package dd.kms.marple.impl.gui.inspector.views.fieldview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.gui.filters.ValueFilters;
import dd.kms.marple.impl.gui.table.*;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.common.FieldScannerBuilder;
import dd.kms.zenodot.api.common.GeneralizedField;
import dd.kms.zenodot.api.common.StaticMode;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;

public class FieldTable extends JPanel implements ObjectView
{
	private final ListBasedTable<GeneralizedField>	table;

	private Object									object;
	private final InspectionContext					context;

	public FieldTable(Object object, InspectionContext context) {
		super(new GridBagLayout());
		this.object = object;
		this.context = context;

		FieldScanner fieldScanner = FieldScannerBuilder.create().staticMode(StaticMode.BOTH).build();
		List<GeneralizedField> fields = fieldScanner.getFields(object.getClass());

		List<ColumnDescription<GeneralizedField>> columnDescriptions = createColumnDescriptions();

		table = new ListBasedTable<>(fields, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new ActionProviderRenderer());
		internalTable.setDefaultRenderer(Member.class, new MemberRenderer());
		internalTable.setDefaultRenderer(Class.class, new ClassRenderer(context));

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));
	}

	@Override
	public String getViewName() {
		return "Detailed Field View";
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

	private List<ColumnDescription<GeneralizedField>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<GeneralizedField>("Name",		String.class,			field -> field.getName()					).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<GeneralizedField>("Value",		ActionProvider.class, 	field -> getFieldValueActionProvider(field)	).valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<GeneralizedField>("Type",		Class.class,			field -> field.getType()					).valueFilter(ValueFilters.createWildcardFilter(o -> context.getDisplayText(o))).build(),
			new ColumnDescriptionBuilder<GeneralizedField>("Class",		Class.class,			field -> field.getDeclaringClass()			).valueFilter(ValueFilters.createSelectionFilter(context)).build(),
			new ColumnDescriptionBuilder<GeneralizedField>("Modifier",	Member.class,			field -> field								).valueFilter(ValueFilters.createModifierFilter(true)).build()
		);
	}

	private ActionProvider getFieldValueActionProvider(GeneralizedField field) {
		String fieldName = field.getName();
		Object fieldValue = ReflectionUtils.getFieldValue(field, object);
		return new ActionProviderBuilder(context.getDisplayText(fieldValue), fieldValue, context)
				.evaluateAs(fieldName, object)
				.build();
	}

	@Override
	public void dispose() {
		object = null;
	}
}
