package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.util.List;

import dd.kms.marple.impl.actions.ActionProvider;

interface MoreChildrenTreeNode extends InspectionTreeNode
{
	List<InspectionTreeNode> getHiddenChildren();

	@Override
	default ActionProvider getActionProvider() {
		return null;
	}

	@Override
	default String getFullText() {
		return "more ...";
	}
}
