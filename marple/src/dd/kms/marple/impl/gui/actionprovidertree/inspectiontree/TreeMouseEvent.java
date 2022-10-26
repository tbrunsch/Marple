package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;

class TreeMouseEvent
{
	private final JTree			tree;
	private final MouseEvent	mouseEvent;

	TreeMouseEvent(JTree tree, MouseEvent mouseEvent) {
		this.tree = tree;
		this.mouseEvent = mouseEvent;
	}

	InspectionTreeModel getTreeModel() {
		TreeModel model = tree.getModel();
		return model instanceof InspectionTreeModel ? (InspectionTreeModel) model : null;
	}

	TreePath getPath() {
		Point pos = mouseEvent.getPoint();
		return tree.getPathForLocation(pos.x, pos.y);
	}

	TreePath getParentPath() {
		TreePath path = getPath();
		return path != null ? path.getParentPath() : null;
	}

	InspectionTreeNode getNode() {
		TreePath path = getPath();
		return getNodeFromPath(path);
	}

	InspectionTreeNode getParentNode() {
		TreePath parentPath = getParentPath();
		return getNodeFromPath(parentPath);
	}

	MouseEvent getMouseEvent() {
		return mouseEvent;
	}

	private InspectionTreeNode getNodeFromPath(TreePath path) {
		if (path == null) {
			return null;
		}
		Object node = path.getLastPathComponent();
		return node instanceof InspectionTreeNode ? (InspectionTreeNode) node : null;
	}
}
