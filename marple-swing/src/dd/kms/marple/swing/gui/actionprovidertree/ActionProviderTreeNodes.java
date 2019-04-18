package dd.kms.marple.swing.gui.actionprovidertree;

import dd.kms.marple.actions.ActionProvider;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;

class ActionProviderTreeNodes
{
	static @Nullable ActionProvider getActionProvider(MouseEvent e) {
		Component component = e.getComponent();
		if (!(component instanceof JTree)) {
			return null;
		}
		JTree tree = (JTree) component;
		TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		if (path == null) {
			return null;
		}
		Object node = path.getLastPathComponent();
		return node instanceof ActionProviderTreeNode
				? ((ActionProviderTreeNode) node).getActionProvider()
				: null;
	}
}
