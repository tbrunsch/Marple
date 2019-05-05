package dd.kms.marple.gui.actionprovidertree.inspectiontree;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.List;

class InspectionTreeModel implements TreeModel
{
	private final InspectionTreeNode	root;

	InspectionTreeModel(InspectionTreeNode root) {
		this.root = root;
	}

	@Override
	public InspectionTreeNode getRoot() {
		return root;
	}

	@Override
	public InspectionTreeNode getChild(Object parent, int index) {
		List<? extends InspectionTreeNode> children = getChildren(parent);
		return 0 <= index && index < children.size() ? children.get(index) : null;
	}

	@Override
	public int getChildCount(Object parent) {
		return getChildren(parent).size();
	}

	@Override
	public boolean isLeaf(Object node) {
		return getChildren(node).isEmpty();
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return child instanceof InspectionTreeNode ? ((InspectionTreeNode) child).getChildIndex() : -1;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		/* do nothing */
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		/* do nothing */
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		/* do nothing */
	}
	List<? extends InspectionTreeNode> getChildren(Object parent) {
		return parent instanceof InspectionTreeNode
				? ((InspectionTreeNode) parent).getChildren()
				: Collections.emptyList();
	}
}
