package dd.kms.marple.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntFunction;

import static dd.kms.marple.gui.actionprovidertree.inspectiontree.ListTreeNode.RANGE_SIZE_BASE;

class ListIndexRangeTreeNode extends AbstractInspectionTreeNode
{
	/**
	 * Creates a node representing the indices from rangeBeginIndex (inclusive) to rangeEndIndex (exclusive).
	 */
	static InspectionTreeNode createRangeNode(int childIndex, Object container, TypeInfo typeInfo, List<?> list, int rangeBeginIndex, int rangeEndIndex, InspectionContext inspectionContext) {
		int rangeSize = rangeEndIndex - rangeBeginIndex;
		if (rangeSize == 1) {
			int index = rangeBeginIndex;
			Object element = list.get(index);
			TypeInfo elementTypeInfo = element == null ? InfoProvider.NO_TYPE : typeInfo.resolveType(element.getClass());
			return InspectionTreeNodes.create(childIndex, "[" + index + "]", element, elementTypeInfo, true, inspectionContext);
		}
		return new ListIndexRangeTreeNode(childIndex, container, typeInfo, list, rangeBeginIndex, rangeEndIndex, inspectionContext);
	}

	private final Object 			container;
	private final TypeInfo			typeInfo;
	private final List<?>			list;
	private final int				rangeBeginIndex;
	private final int				rangeEndIndex;
	private final InspectionContext	inspectionContext;

	ListIndexRangeTreeNode(int childIndex, Object container, TypeInfo typeInfo, List<?> list, int rangeBeginIndex, int rangeEndIndex, InspectionContext inspectionContext) {
		super(childIndex);
		this.container = container;
		this.typeInfo = typeInfo;
		this.list = list;
		this.rangeBeginIndex = rangeBeginIndex;
		this.rangeEndIndex = rangeEndIndex;
		this.inspectionContext = inspectionContext;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		int rangeSize = calculateRangeSize();
		int rangeBeginIndex = this.rangeBeginIndex;
		ImmutableList.Builder<InspectionTreeNode> childBuilder = ImmutableList.builder();
		int childIndex = 0;
		while (rangeBeginIndex < rangeEndIndex) {
			int rangeEndIndex = Math.min(rangeBeginIndex + rangeSize, this.rangeEndIndex);
			InspectionTreeNode node = createRangeNode(childIndex, rangeBeginIndex, rangeEndIndex);
			childBuilder.add(node);
			childIndex++;
			rangeBeginIndex = rangeEndIndex;
		}
		return childBuilder.build();
	}

	private int calculateRangeSize() {
		int logRangeSize = (int) Math.ceil(Math.log(rangeEndIndex - rangeBeginIndex)/Math.log(RANGE_SIZE_BASE)) - 1;
		int rangeSize = 1;
		for (int i = 0; i < logRangeSize; i++) {
			rangeSize *= RANGE_SIZE_BASE;
		}
		return rangeSize;
	}

	private InspectionTreeNode createRangeNode(int childIndex, int rangeBeginIndex, int rangeEndIndex) {
		return createRangeNode(childIndex, container, typeInfo, list, rangeBeginIndex, rangeEndIndex, inspectionContext);
	}

	@Override
	public @Nullable ActionProvider getActionProvider() {
		/* no reasonable actions possible for a range */
		return null;
	}

	@Override
	public String toString() {
		return "[" + rangeBeginIndex + ".." + (rangeEndIndex-1) + "]";
	}
}
