package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNode;

import java.util.List;

interface InspectionTreeNode extends ActionProviderTreeNode
{
	int getChildIndex(Object child);
	List<InspectionTreeNode> getChildren();
	default void handleLeftMouseButtonClicked(TreeMouseEvent e) {}
}
