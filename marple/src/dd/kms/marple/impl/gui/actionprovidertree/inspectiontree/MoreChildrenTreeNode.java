package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import dd.kms.marple.impl.actions.ActionProvider;

import java.util.List;

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
