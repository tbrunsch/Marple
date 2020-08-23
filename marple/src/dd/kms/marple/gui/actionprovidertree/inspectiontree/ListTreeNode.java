package dd.kms.marple.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.actions.Actions;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Node that represents both, lists and arrays
 */
class ListTreeNode extends AbstractInspectionTreeNode
{
	static final int	RANGE_SIZE_BASE	= 10;

	private final @Nullable String	displayKey;
	private final ObjectInfo		containerInfo;
	private final List<?>			list;
	private final InspectionContext	inspectionContext;

	ListTreeNode(int childIndex, @Nullable String displayKey, ObjectInfo containerInfo, List<?> list, InspectionContext inspectionContext) {
		super(childIndex);
		this.displayKey = displayKey;
		this.containerInfo = containerInfo;
		this.list = list;
		this.inspectionContext = inspectionContext;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		int rangeSize = 1;
		int rangeBeginIndex = 0;
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		int childIndex = 0;
		int firstIndexWithLargerRange = RANGE_SIZE_BASE;
		while (true) {
			int listSize = list.size();
			if (!(rangeBeginIndex < listSize)) break;
			int rangeEndIndex = Math.min(rangeBeginIndex + rangeSize, listSize);
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
		return ListIndexRangeTreeNode.createRangeNode(childIndex, containerInfo, list, rangeBeginIndex, rangeEndIndex, inspectionContext);
	}

	@Override
	public ActionProvider getActionProvider() {
		return new ActionProviderBuilder(toString(), containerInfo, inspectionContext)
			.suggestVariableName(displayKey)
			.build();
	}

	@Override
	public String toString() {
		String valueDisplayText = Actions.trimName(inspectionContext.getDisplayText(containerInfo)) + " size = " + list.size();
		return displayKey == null ? valueDisplayText : displayKey + " = " + valueDisplayText;
	}
}
