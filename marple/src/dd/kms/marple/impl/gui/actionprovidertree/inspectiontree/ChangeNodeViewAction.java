package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import dd.kms.marple.api.actions.InspectionAction;

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
	public boolean isDefaultAction() {
		return false;
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
		InspectionTreeNodes.replaceNode(treeMouseEvent.getParentPath(), treeMouseEvent.getParentNode(), node, Collections.singletonList(alternativeNode), treeMouseEvent.getTreeModel());
	}
}
