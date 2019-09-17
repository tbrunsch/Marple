package dd.kms.marple.gui.inspector.views.fieldview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.gui.filters.ValueFilters;
import dd.kms.marple.gui.table.*;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.FieldScanner;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;

public class FieldTable extends JPanel
{
	private final ListBasedTable<FieldInfo>	table;

	private final ObjectInfo				objectInfo;
	private final InspectionContext			inspectionContext;

	public FieldTable(ObjectInfo objectInfo, InspectionContext inspectionContext) {
		super(new GridBagLayout());
		this.objectInfo = objectInfo;
		this.inspectionContext = inspectionContext;

		List<FieldInfo> fieldInfos = InfoProvider.getFieldInfos(ReflectionUtils.getRuntimeTypeInfo(objectInfo), new FieldScanner());

		List<ColumnDescription<FieldInfo>> columnDescriptions = createColumnDescriptions();

		table = new ListBasedTable<>(fieldInfos, columnDescriptions);
		JTable internalTable = table.getInternalTable();
		internalTable.getColumnModel().getColumn(1).setCellRenderer(new ActionProviderRenderer());
		internalTable.setDefaultRenderer(AccessModifier.class, new AccessModifierRenderer());

		add(table, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));
	}

	private List<ColumnDescription<FieldInfo>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<FieldInfo>("Name",		String.class,			fieldInfo -> fieldInfo.getName())						.valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<FieldInfo>("Value",	ActionProvider.class, 	fieldInfo -> getFieldValueActionProvider(fieldInfo))	.valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<FieldInfo>("Type",		TypeInfo.class,			fieldInfo -> fieldInfo.getType())						.valueFilter(ValueFilters.createWildcardFilter()).build(),
			new ColumnDescriptionBuilder<FieldInfo>("Class",	TypeInfo.class,			fieldInfo -> fieldInfo.getDeclaringType())				.valueFilter(ValueFilters.createSelectionFilter(inspectionContext)).build(),
			new ColumnDescriptionBuilder<FieldInfo>("Modifier",	AccessModifier.class,	fieldInfo -> fieldInfo.getAccessModifier())				.valueFilter(ValueFilters.createMinimumAccessLevelFilter()).build()
		);
	}

	private ActionProvider getFieldValueActionProvider(FieldInfo fieldInfo) {
		String fieldName = fieldInfo.getName();
		ObjectInfo fieldValueInfo = getFieldValue(fieldInfo);
		return new ActionProviderBuilder(inspectionContext.getDisplayText(fieldValueInfo), fieldValueInfo, inspectionContext)
				.evaluateAs(fieldName, objectInfo)
				.build();
	}

	private ObjectInfo getFieldValue(FieldInfo fieldInfo) {
		return ReflectionUtils.OBJECT_INFO_PROVIDER.getFieldValueInfo(objectInfo.getObject(), fieldInfo);
	}
}
