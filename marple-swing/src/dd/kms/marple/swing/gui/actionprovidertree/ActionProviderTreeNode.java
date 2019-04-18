package dd.kms.marple.swing.gui.actionprovidertree;

import dd.kms.marple.actions.ActionProvider;

import javax.annotation.Nullable;
import javax.swing.tree.TreeNode;

public interface ActionProviderTreeNode extends TreeNode
{
	@Nullable ActionProvider getActionProvider();
}
