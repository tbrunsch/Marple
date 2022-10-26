package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.util.Iterator;
import java.util.List;

class MoreIterableChildrenTreeNode extends MoreChildrenTreeNode
{
	private final Iterator<List<InspectionTreeNode>>	childIterator;
	private final int									maxNumVisibleChildren;

	MoreIterableChildrenTreeNode(Iterator<List<InspectionTreeNode>> childIterator, int maxNumVisibleChildren) {
		this.childIterator = childIterator;
		this.maxNumVisibleChildren = maxNumVisibleChildren;
	}

	@Override
	public List<InspectionTreeNode> getHiddenChildren() {
		return InspectionTreeNodes.getChildren(childIterator, maxNumVisibleChildren);
	}
}
