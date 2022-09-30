package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.common.FieldScannerBuilder;
import dd.kms.zenodot.api.common.StaticMode;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;

class DefaultObjectTreeNode extends AbstractInspectionTreeNode
{
	private final @Nullable String	displayKey;
	private final Object			object;
	private final InspectionContext	context;

	DefaultObjectTreeNode(@Nullable String displayKey, Object object, InspectionContext context) {
		this.displayKey = displayKey;
		this.object = object;
		this.context = context;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		if (!ReflectionUtils.isObjectInspectable(object)) {
			return ImmutableList.of();
		}
		FieldScanner fieldScanner = FieldScannerBuilder.create().staticMode(StaticMode.NON_STATIC).build();
		List<Field> fields = fieldScanner.getFields(object.getClass());
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		for (Field field : fields) {
			Object fieldValue = ReflectionUtils.getFieldValue(field, object);
			InspectionTreeNode child = InspectionTreeNodes.create(field.getName(), fieldValue, context);
			childBuilder.add(child);
		}
		return childBuilder.build();
	}

	@Override
	public ActionProvider getActionProvider() {
		return new ActionProviderBuilder(toString(), object, context)
			.suggestVariableName(displayKey)
			.build();
	}

	@Override
	public String getFullText() {
		String valueDisplayText = context.getDisplayText(object);
		return displayKey == null
			? valueDisplayText + " (" + (object != null ? context.getDisplayText(object.getClass()) : null) + ")"
			: displayKey + " = " + valueDisplayText;
	}
}
