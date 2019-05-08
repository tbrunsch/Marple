package dd.kms.marple.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.actions.Actions;
import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;

class DefaultObjectTreeNode extends AbstractInspectionTreeNode
{
	private final @Nullable String	displayKey;
	private final @Nullable Object	object;
	private final TypeInfo			typeInfo;
	private final InspectionContext	inspectionContext;

	DefaultObjectTreeNode(int childIndex, @Nullable String displayKey, Object object, TypeInfo typeInfo, InspectionContext inspectionContext) {
		super(childIndex);
		this.displayKey = displayKey;
		this.object = object;
		this.typeInfo = typeInfo;
		this.inspectionContext = inspectionContext;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		if (object == null || Primitives.unwrap(object.getClass()).isPrimitive()) {
			return ImmutableList.of();
		}
		List<Field> fields = ReflectionUtils.getFields(typeInfo.getRawType(), false);
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		int childIndex = 0;
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			field.setAccessible(true);
			Object fieldValue;
			try {
				fieldValue = field.get(object);
			} catch (IllegalAccessException e) {
				System.err.println(e.getMessage());
				fieldValue = null;
			}
			TypeInfo childTypeInfo = fieldValue == null ? InfoProvider.NO_TYPE : typeInfo.resolveType(fieldValue.getClass());
			InspectionTreeNode child = InspectionTreeNodes.create(childIndex++, field.getName(), fieldValue, childTypeInfo, inspectionContext);
			childBuilder.add(child);
		}
		return childBuilder.build();
	}

	@Override
	public ActionProvider getActionProvider() {
		return new ActionProviderBuilder(toString(), object, inspectionContext)
			.suggestVariableName(displayKey)
			.build();
	}

	@Override
	public String toString() {
		String valueDisplayText = inspectionContext.getDisplayText(object);
		String fullNodeDisplayText = displayKey == null
			? valueDisplayText + " (" + typeInfo.getType() + ")"
			: displayKey + " = " + valueDisplayText;
		return Actions.trimName(fullNodeDisplayText);
	}
}
