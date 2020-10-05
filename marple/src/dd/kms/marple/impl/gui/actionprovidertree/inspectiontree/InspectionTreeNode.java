package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.util.List;

import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNode;

interface InspectionTreeNode extends ActionProviderTreeNode
{
	int getChildIndex(Object child);
	List<InspectionTreeNode> getChildren();
}
