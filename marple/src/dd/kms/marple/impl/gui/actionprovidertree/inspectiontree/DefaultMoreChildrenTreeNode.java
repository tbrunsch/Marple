package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class DefaultMoreChildrenTreeNode extends AbstractInspectionTreeNode implements MoreChildrenTreeNode
{
	private final Iterator<List<InspectionTreeNode>>	childIterator;
	private final int									maxNumVisibleChildren;

	DefaultMoreChildrenTreeNode(Iterator<List<InspectionTreeNode>> childIterator, int maxNumVisibleChildren) {
		this.childIterator = childIterator;
		this.maxNumVisibleChildren = maxNumVisibleChildren;
	}

	@Override
	List<? extends InspectionTreeNode> doGetChildren() {
		return Collections.emptyList();
	}

	@Override
	public List<InspectionTreeNode> getHiddenChildren() {
		return InspectionTreeNodes.getChildren(childIterator, maxNumVisibleChildren);
	}
}
