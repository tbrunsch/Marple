package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.wrappers.FieldInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

class DefaultObjectTreeNode extends AbstractInspectionTreeNode
{
	private final @Nullable String	displayKey;
	private final ObjectInfo		objectInfo;
	private final InspectionContext	context;

	DefaultObjectTreeNode(@Nullable String displayKey, ObjectInfo objectInfo, InspectionContext context) {
		this.displayKey = displayKey;
		this.objectInfo = objectInfo;
		this.context = context;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		if (!ReflectionUtils.isObjectInspectable(objectInfo.getObject())) {
			return ImmutableList.of();
		}
		List<FieldInfo> fieldInfos = InfoProvider.getFieldInfos(ReflectionUtils.getRuntimeTypeInfo(objectInfo), new FieldScanner());
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		for (FieldInfo fieldInfo : fieldInfos) {
			ObjectInfo fieldValueInfo = ReflectionUtils.OBJECT_INFO_PROVIDER.getFieldValueInfo(objectInfo.getObject(), fieldInfo);
			InspectionTreeNode child = InspectionTreeNodes.create(fieldInfo.getName(), fieldValueInfo, context);
			childBuilder.add(child);
		}
		return childBuilder.build();
	}

	@Override
	public ActionProvider getActionProvider() {
		return new ActionProviderBuilder(toString(), objectInfo, context)
			.suggestVariableName(displayKey)
			.build();
	}

	@Override
	public String getFullText() {
		String valueDisplayText = context.getDisplayText(objectInfo);
		return displayKey == null
			? valueDisplayText + " (" + ReflectionUtils.getRuntimeTypeInfo(objectInfo) + ")"
			: displayKey + " = " + valueDisplayText;
	}
}
