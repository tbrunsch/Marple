package dd.kms.marple.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.actions.Actions;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.zenodot.common.FieldScanner;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import java.util.List;

class DefaultObjectTreeNode extends AbstractInspectionTreeNode
{
	private final @Nullable String	displayKey;
	private final ObjectInfo		objectInfo;
	private final InspectionContext	inspectionContext;

	DefaultObjectTreeNode(int childIndex, @Nullable String displayKey, ObjectInfo objectInfo, InspectionContext inspectionContext) {
		super(childIndex);
		this.displayKey = displayKey;
		this.objectInfo = objectInfo;
		this.inspectionContext = inspectionContext;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		if (!ReflectionUtils.isObjectInspectable(objectInfo.getObject())) {
			return ImmutableList.of();
		}
		List<FieldInfo> fieldInfos = InfoProvider.getFieldInfos(ReflectionUtils.getRuntimeTypeInfo(objectInfo), new FieldScanner());
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		int childIndex = 0;
		for (FieldInfo fieldInfo : fieldInfos) {
			ObjectInfo fieldValueInfo = ReflectionUtils.OBJECT_INFO_PROVIDER.getFieldValueInfo(objectInfo.getObject(), fieldInfo);
			InspectionTreeNode child = InspectionTreeNodes.create(childIndex++, fieldInfo.getName(), fieldValueInfo, true, inspectionContext);
			childBuilder.add(child);
		}
		return childBuilder.build();
	}

	@Override
	public ActionProvider getActionProvider() {
		return new ActionProviderBuilder(toString(), objectInfo, inspectionContext)
			.suggestVariableName(displayKey)
			.build();
	}

	@Override
	public String toString() {
		String valueDisplayText = inspectionContext.getDisplayText(objectInfo);
		String fullNodeDisplayText = displayKey == null
			? valueDisplayText + " (" + ReflectionUtils.getRuntimeTypeInfo(objectInfo) + ")"
			: displayKey + " = " + valueDisplayText;
		return Actions.trimName(fullNodeDisplayText);
	}
}
