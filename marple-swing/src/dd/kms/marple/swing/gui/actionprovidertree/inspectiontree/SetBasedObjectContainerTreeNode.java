package dd.kms.marple.swing.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.swing.actions.Actions;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Node that represents both, lists and arrays
 */
class SetBasedObjectContainerTreeNode extends AbstractInspectionTreeNode
{
	static final int	RANGE_SIZE_BASE	= 10;

	private final @Nullable String					fieldName;
	private final Object 							container;
	private final TypeInfo							typeInfo;
	private final Set<?>							keys;
	private final @Nullable Function<Object, ?>		elementAccessor;
	private final InspectionContext<Component>		inspectionContext;

	SetBasedObjectContainerTreeNode(int childIndex, @Nullable String displayKey, Object container, TypeInfo typeInfo, Set<?> keys, @Nullable Function<Object, ?> elementAccessor, InspectionContext<Component> inspectionContext) {
		super(childIndex);
		this.fieldName = displayKey;
		this.container = container;
		this.typeInfo = typeInfo;
		this.keys = keys;
		this.elementAccessor = elementAccessor;
		this.inspectionContext = inspectionContext;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		int numChildren = 0;
		int entryIndex = 0;
		for (Object key : keys) {
			InspectionTreeNode keyNode = createKeyNode(numChildren++, entryIndex, key);
			childBuilder.add(keyNode);
			if (withValueNodes()) {
				Object value = elementAccessor.apply(key);
				InspectionTreeNode valueNode = createValueNode(numChildren++, entryIndex, value);
				childBuilder.add(valueNode);
			}
			entryIndex++;
		}
		return childBuilder.build();
	}

	@Override
	public ActionProvider getActionProvider() {
		InspectionAction inspectObjectAction = inspectionContext.createInspectObjectAction(container);
		InspectionAction addVariableAction = Actions.createAddVariableAction(fieldName, container, inspectionContext);
		InspectionAction evaluateAsThisAction = inspectionContext.createEvaluateAsThisAction(container);
		return ActionProvider.of(toString(), inspectObjectAction, addVariableAction, evaluateAsThisAction);
	}

	@Override
	public String toString() {
		String valueDisplayText = Actions.trimName(inspectionContext.getDisplayText(container)) + " size = " + keys.size();
		return fieldName == null ? valueDisplayText : fieldName + " = " + valueDisplayText;
	}

	private InspectionTreeNode createKeyNode(int childIndex, int entryIndex, Object key) {
		String displayText = getKeyNodeDisplayText(entryIndex);
		TypeInfo typeInfo = key == null ? TypeInfo.NONE : this.typeInfo.resolveType(key.getClass());
		return InspectionTreeNodes.create(childIndex, displayText, key, typeInfo, inspectionContext);
	}

	private InspectionTreeNode createValueNode(int childIndex, int entryIndex, Object value) {
		String displayText = getValueNodeDisplayText(entryIndex);
		TypeInfo typeInfo = value == null ? TypeInfo.NONE : this.typeInfo.resolveType(value.getClass());
		return InspectionTreeNodes.create(childIndex, displayText, value, typeInfo, inspectionContext);
	}

	private String getKeyNodeDisplayText(int entryIndex) {
		return withValueNodes() ? "[key" + entryIndex + "]" : "[entry" + entryIndex + "]";
	}

	private String getValueNodeDisplayText(int entryIndex) {
		return "[value" + entryIndex + "]";
	}

	private boolean withValueNodes() {
		return elementAccessor != null;
	}
}
