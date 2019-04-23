package dd.kms.marple.swing.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.swing.actions.Actions;
import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;

class DefaultObjectTreeNode extends AbstractInspectionTreeNode
{
	private final @Nullable String				fieldName;
	private final @Nullable Object				object;
	private final TypeInfo						typeInfo;
	private final InspectionContext<Component>	inspectionContext;

	DefaultObjectTreeNode(int childIndex, @Nullable String fieldName, Object object, TypeInfo typeInfo, InspectionContext<Component> inspectionContext) {
		super(childIndex);
		this.fieldName = fieldName;
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
			TypeInfo childTypeInfo = typeInfo.resolveType(field.getType());
			InspectionTreeNode child = InspectionTreeNodes.create(childIndex++, field.getName(), fieldValue, childTypeInfo, inspectionContext);
			childBuilder.add(child);
		}
		return childBuilder.build();
	}

	@Override
	public ActionProvider getActionProvider() {
		if (object == null) {
			return null;
		}
		InspectionAction inspectObjectAction = inspectionContext.createInspectObjectAction(object);
		InspectionAction highlightComponentAction = object instanceof Component
			? inspectionContext.createHighlightComponentAction((Component) object)
			: null;
		InspectionAction addVariableAction = Actions.createAddVariableAction(fieldName, object, inspectionContext);
		InspectionAction evaluateAsThisAction = inspectionContext.createEvaluateAsThisAction(object);
		return ActionProvider.of(toString(), inspectObjectAction, highlightComponentAction, addVariableAction, evaluateAsThisAction);
	}

	@Override
	public String toString() {
		String valueDisplayText = inspectionContext.getDisplayText(object);
		String fullNodeDisplayText = fieldName == null
			? valueDisplayText + " (" + typeInfo.getType() + ")"
			: fieldName + " = " + valueDisplayText;
		return Actions.trimName(fullNodeDisplayText);
	}
}
