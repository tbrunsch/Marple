package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.util.LinkedList;
import java.util.List;

abstract class AbstractInspectionTreeNode implements InspectionTreeNode
{
	private List<InspectionTreeNode>	cachedChildren;

	abstract List<? extends InspectionTreeNode> doGetChildren();

	@Override
	public int getChildIndex(Object child) {
		return cachedChildren.indexOf(child);
	}

	@Override
	public List<InspectionTreeNode> getChildren() {
		if (cachedChildren == null) {
			cachedChildren = new LinkedList<>();
			cachedChildren.addAll(doGetChildren());
		}
		return cachedChildren;
	}

	@Override
	public final String toString() {
		return getTrimmedText();
	}
}
