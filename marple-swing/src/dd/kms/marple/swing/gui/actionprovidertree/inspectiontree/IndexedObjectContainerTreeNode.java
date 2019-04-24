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
import java.util.function.IntFunction;

/**
 * Node that represents both, lists and arrays
 */
class IndexedObjectContainerTreeNode extends AbstractInspectionTreeNode
{
	static final int	RANGE_SIZE_BASE	= 10;

	private final @Nullable String				displayKey;
	private final Object 						container;
	private final TypeInfo						typeInfo;
	private final int							containerSize;
	private final IntFunction<Object>			elementAccessor;
	private final InspectionContext<Component>	inspectionContext;

	IndexedObjectContainerTreeNode(int childIndex, @Nullable String displayKey, Object container, TypeInfo typeInfo, int containerSize, IntFunction<Object> elementAccessor, InspectionContext<Component> inspectionContext) {
		super(childIndex);
		this.displayKey = displayKey;
		this.container = container;
		this.typeInfo = typeInfo;
		this.containerSize = containerSize;
		this.elementAccessor = elementAccessor;
		this.inspectionContext = inspectionContext;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		int rangeSize = 1;
		int rangeBeginIndex = 0;
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		int childIndex = 0;
		int firstIndexWithLargerRange = RANGE_SIZE_BASE;
		while (rangeBeginIndex < containerSize) {
			int rangeEndIndex = Math.min(rangeBeginIndex + rangeSize, containerSize);
			InspectionTreeNode node = createRangeNode(childIndex, rangeBeginIndex, rangeEndIndex);
			childBuilder.add(node);
			childIndex++;
			rangeBeginIndex = rangeEndIndex;
			if (rangeBeginIndex == firstIndexWithLargerRange) {
				firstIndexWithLargerRange *= RANGE_SIZE_BASE;
				rangeSize *= RANGE_SIZE_BASE;
			}
		}
		return childBuilder.build();
	}

	private InspectionTreeNode createRangeNode(int childIndex, int rangeBeginIndex, int rangeEndIndex) {
		return IndexedObjectContainerIndexRangeTreeNode.createRangeNode(childIndex, container, typeInfo, elementAccessor, rangeBeginIndex, rangeEndIndex, inspectionContext);
	}

	@Override
	public ActionProvider getActionProvider() {
		InspectionAction inspectObjectAction = inspectionContext.createInspectObjectAction(container);
		InspectionAction addVariableAction = Actions.createAddVariableAction(displayKey, container, inspectionContext);
		InspectionAction evaluateAsThisAction = inspectionContext.createEvaluateAsThisAction(container);
		return ActionProvider.of(toString(), inspectObjectAction, addVariableAction, evaluateAsThisAction);
	}

	@Override
	public String toString() {
		String valueDisplayText = Actions.trimName(inspectionContext.getDisplayText(container)) + " size = " + containerSize;
		return displayKey == null ? valueDisplayText : displayKey + " = " + valueDisplayText;
	}
}
