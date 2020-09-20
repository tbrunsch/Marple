package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.util.List;

abstract class AbstractInspectionTreeNode implements InspectionTreeNode
{
	private final int							childIndex;

	private List<? extends InspectionTreeNode>	cachedChildren;

	AbstractInspectionTreeNode(int childIndex) {
		this.childIndex = childIndex;
	}

	abstract List<? extends InspectionTreeNode> doGetChildren();

	@Override
	public int getChildIndex() {
		return childIndex;
	}

	@Override
	public List<? extends InspectionTreeNode> getChildren() {
		if (cachedChildren == null) {
			cachedChildren = doGetChildren();
		}
		return cachedChildren;
	}
}
