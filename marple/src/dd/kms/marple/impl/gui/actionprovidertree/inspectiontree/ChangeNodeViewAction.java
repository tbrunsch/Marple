package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import dd.kms.marple.api.actions.InspectionAction;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.Collections;

class ChangeNodeViewAction implements InspectionAction
{
	private final AbstractInspectionTreeNode	node;
	private final ViewOption					viewOption;
	private final TreeMouseEvent				treeMouseEvent;

	ChangeNodeViewAction(AbstractInspectionTreeNode node, ViewOption viewOption, TreeMouseEvent treeMouseEvent) {
		this.node = node;
		this.viewOption = viewOption;
		this.treeMouseEvent = treeMouseEvent;
	}

	@Override
	public String getName() {
		return viewOption.getText();
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		InspectionTreeNode alternativeNode = InspectionTreeNodes.create(node.getDisplayKey(), node.getObject(), node.getContext(), viewOption);
		TreePath parentPath = treeMouseEvent.getParentPath();
		InspectionTreeNodes.replaceNode(parentPath, treeMouseEvent.getParentNode(), node, Collections.singletonList(alternativeNode), treeMouseEvent.getTreeModel());
		JTree tree = treeMouseEvent.getTree();
		TreePath alternativeNodePath = parentPath != null
				? parentPath.pathByAddingChild(alternativeNode)
				: new TreePath(alternativeNode);
		tree.expandPath(alternativeNodePath);
	}
}
