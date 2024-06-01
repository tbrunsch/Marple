package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import dd.kms.marple.api.actions.InspectionAction;

import java.util.List;

class ShowNextElementsAction implements InspectionAction
{
	private final MoreChildrenTreeNode	node;
	private final TreeMouseEvent		treeMouseEvent;

	ShowNextElementsAction(MoreChildrenTreeNode node, TreeMouseEvent treeMouseEvent) {
		this.node = node;
		this.treeMouseEvent = treeMouseEvent;
	}

	@Override
	public String getName() {
		return "Show next elements";
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
		List<InspectionTreeNode> hiddenChildren = node.getHiddenChildren();
		InspectionTreeNodes.replaceNode(treeMouseEvent.getParentPath(), treeMouseEvent.getParentNode(), node, hiddenChildren, treeMouseEvent.getTreeModel());
	}
}
