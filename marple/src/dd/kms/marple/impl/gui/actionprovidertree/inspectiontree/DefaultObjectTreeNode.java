package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.common.FieldScannerBuilder;
import dd.kms.zenodot.api.common.GeneralizedField;
import dd.kms.zenodot.api.common.StaticMode;

import javax.annotation.Nullable;
import java.util.List;

class DefaultObjectTreeNode extends AbstractInspectionTreeNode
{
	DefaultObjectTreeNode(@Nullable String displayKey, Object object, InspectionContext context, List<ViewOption> alternativeViews) {
		super(displayKey, object, context, alternativeViews);
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		Object object = getObject();
		if (!ReflectionUtils.isObjectInspectable(object)) {
			return ImmutableList.of();
		}
		FieldScanner fieldScanner = FieldScannerBuilder.create().staticMode(StaticMode.NON_STATIC).build();
		List<GeneralizedField> fields = fieldScanner.getFields(object.getClass());
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		for (GeneralizedField field : fields) {
			Object fieldValue = ReflectionUtils.getFieldValue(field, object);
			InspectionTreeNode child = InspectionTreeNodes.create(field.getName(), fieldValue, getContext());
			childBuilder.add(child);
		}
		return childBuilder.build();
	}

	@Override
	public String getFullText() {
		String displayKey = getDisplayKey();
		Object object = getObject();
		InspectionContext context = getContext();
		String valueDisplayText = context.getDisplayText(object);
		return displayKey == null
			? valueDisplayText + " (" + (object != null ? context.getDisplayText(object.getClass()) : null) + ")"
			: displayKey + " = " + valueDisplayText;
	}
}
