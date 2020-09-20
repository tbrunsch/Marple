package dd.kms.marple.impl.gui.search;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.function.Consumer;

class FullPathMouseMotionListener extends MouseMotionAdapter
{
	private final Consumer<String>	pathConsumer;

	FullPathMouseMotionListener(Consumer<String> pathConsumer) {
		this.pathConsumer = pathConsumer;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		SearchNode node = getSearchNode(e);
		String path = node == null ? null : node.getFullPathAsString();
		pathConsumer.accept(path);
	}

	private SearchNode getSearchNode(MouseEvent e) {
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
		return node instanceof SearchNode ? (SearchNode) node : null;
	}
}
