package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class InspectionTreeModel implements TreeModel
{
	private InspectionTreeNode				root;
	private final Set<TreeModelListener>	listeners	= new HashSet<>();

	InspectionTreeModel(InspectionTreeNode root) {
		this.root = root;
	}

	void setRoot(InspectionTreeNode root) {
		this.root = root;
	}

	@Override
	public InspectionTreeNode getRoot() {
		return root;
	}

	@Override
	public InspectionTreeNode getChild(Object parent, int index) {
		List<InspectionTreeNode> children = getChildren(parent);
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
		return parent instanceof InspectionTreeNode
				? ((InspectionTreeNode) parent).getChildIndex(child)
				: -1;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		/* do nothing */
	}

	@Override
	public void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}

	void fireTreeNodesRemoved(Object[] path, int[] childIndices, Object[] children) {
		TreeModelEvent e = new TreeModelEvent(this, path, childIndices, children);
		for (TreeModelListener listener : listeners) {
			listener.treeNodesRemoved(e);
		}
	}

	void fireTreeNodesInserted(Object[] path, int[] childIndices, Object[] children) {
		TreeModelEvent e = new TreeModelEvent(this, path, childIndices, children);
		for (TreeModelListener listener : listeners) {
			listener.treeNodesInserted(e);
		}
	}

	void fireTreeStructureChanged() {
		TreeModelEvent e = new TreeModelEvent(this, (Object[]) null, new int[0], new Object[0]);
		for (TreeModelListener listener : listeners) {
			listener.treeStructureChanged(e);
		}
	}

	List<InspectionTreeNode> getChildren(Object parent) {
		return parent instanceof InspectionTreeNode
				? ((InspectionTreeNode) parent).getChildren()
				: Collections.emptyList();
	}
}
