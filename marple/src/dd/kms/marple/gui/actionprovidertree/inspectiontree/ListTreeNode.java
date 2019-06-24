package dd.kms.marple.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.actions.Actions;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Node that represents both, lists and arrays
 */
class ListTreeNode extends AbstractInspectionTreeNode
{
	static final int	RANGE_SIZE_BASE	= 10;

	private final @Nullable String		displayKey;
	private final Object 				container;
	private final TypeInfo				typeInfo;
	private final List<?>				list;
	private final InspectionContext		inspectionContext;

	ListTreeNode(int childIndex, @Nullable String displayKey, Object container, TypeInfo typeInfo, List<?> list, InspectionContext inspectionContext) {
		super(childIndex);
		this.displayKey = displayKey;
		this.container = container;
		this.typeInfo = typeInfo;
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
		return ListIndexRangeTreeNode.createRangeNode(childIndex, container, typeInfo, list, rangeBeginIndex, rangeEndIndex, inspectionContext);
	}

	@Override
	public ActionProvider getActionProvider() {
		return new ActionProviderBuilder(toString(), container, inspectionContext)
			.suggestVariableName(displayKey)
			.build();
	}

	@Override
	public String toString() {
		String valueDisplayText = Actions.trimName(inspectionContext.getDisplayText(container)) + " size = " + list.size();
		return displayKey == null ? valueDisplayText : displayKey + " = " + valueDisplayText;
	}
}
