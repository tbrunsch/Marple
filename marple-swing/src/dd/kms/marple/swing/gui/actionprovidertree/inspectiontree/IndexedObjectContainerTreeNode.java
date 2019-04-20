package dd.kms.marple.swing.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.swing.gui.Actions;
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

	private final @Nullable String				fieldName;
	private final Object 						container;
	private final TypeInfo						typeInfo;
	private final IntFunction<Object>			elementAccessor;
	private final int							containerSize;
	private final InspectionContext<Component>	inspectionContext;

	IndexedObjectContainerTreeNode(int childIndex, @Nullable String fieldName, Object container, TypeInfo typeInfo, IntFunction<Object> elementAccessor, int containerSize, InspectionContext<Component> inspectionContext) {
		super(childIndex);
		this.fieldName = fieldName;
		this.container = container;
		this.typeInfo = typeInfo;
		this.elementAccessor = elementAccessor;
		this.containerSize = containerSize;
		this.inspectionContext = inspectionContext;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		int rangeSize = 1;
		int rangeBeginIndex = 0;
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		int numChildren = 0;
		int firstIndexWithLargerRange = RANGE_SIZE_BASE;
		while (rangeBeginIndex < containerSize) {
			int rangeEndIndex = Math.min(rangeBeginIndex + rangeSize, containerSize);
			InspectionTreeNode node = createRangeNode(numChildren, rangeBeginIndex, rangeEndIndex);
			childBuilder.add(node);
			numChildren++;
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
		InspectionAction evaluateAsThisAction = inspectionContext.createEvaluateAsThisAction(container);
		return ActionProvider.of(toString(), inspectObjectAction, evaluateAsThisAction);
	}

	@Override
	public String toString() {
		String valueDisplayText = Actions.trimName(inspectionContext.getDisplayText(container)) + " size = " + containerSize;
		return fieldName == null ? valueDisplayText : fieldName + " = " + valueDisplayText;
	}
}
