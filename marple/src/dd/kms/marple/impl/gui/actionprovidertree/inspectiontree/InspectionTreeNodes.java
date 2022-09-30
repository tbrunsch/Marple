package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.UniformView;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class InspectionTreeNodes
{
	public static TreeModel createModel(@Nullable String fieldName, Object object, InspectionContext context) {
		InspectionTreeNode treeNode = create(fieldName, object, context);
		return new InspectionTreeModel(treeNode);
	}

	public static void enableMoreChildrenNodeReplacement(JTree tree) {
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TreeModel model = tree.getModel();
				if (!(model instanceof InspectionTreeModel)) {
					return;
				}
				Point pos = e.getPoint();
				TreePath path = tree.getPathForLocation(pos.x, pos.y);
				if (path == null) {
					return;
				}
				TreePath parentPath = path.getParentPath();
				if (parentPath == null) {
					return;
				}
				Object node = path.getLastPathComponent();
				Object parentNode = parentPath.getLastPathComponent();
				if (!(parentNode instanceof InspectionTreeNode)) {
					return;
				}
				if (!(node instanceof MoreChildrenTreeNode)) {
					return;
				}
				InspectionTreeNode parent = (InspectionTreeNode) parentNode;
				int childIndex = parent.getChildIndex(node);
				InspectionTreeModel treeModel = (InspectionTreeModel) model;

				List<InspectionTreeNode> children = parent.getChildren();
				children.remove(node);
				treeModel.fireTreeNodesRemoved(parentPath.getPath(), new int[]{ childIndex }, new Object[]{ node });

				List<InspectionTreeNode> hiddenChildren = ((MoreChildrenTreeNode) node).getHiddenChildren();
				children.addAll(hiddenChildren);
				treeModel.fireTreeNodesInserted(parentPath.getPath(), IntStream.range(childIndex, childIndex + hiddenChildren.size()).toArray(), hiddenChildren.toArray());
			}
		});
	}

	static List<InspectionTreeNode> getChildren(Iterator<List<InspectionTreeNode>> childIterator, int maxNumVisibleChildren) {
		ImmutableList.Builder<InspectionTreeNode> builder = ImmutableList.builder();
		int numChildren = 0;
		while (childIterator.hasNext() && numChildren < maxNumVisibleChildren) {
			List<InspectionTreeNode> furtherChildren = childIterator.next();
			builder.addAll(furtherChildren);
			numChildren += furtherChildren.size();
		}
		if (childIterator.hasNext()) {
			builder.add(new DefaultMoreChildrenTreeNode(childIterator, maxNumVisibleChildren));
		}
		return builder.build();
	}

	static InspectionTreeNode create(@Nullable String displayKey, Object object, InspectionContext context) {
		if (UniformView.canViewAsList(object)) {
			List<?> list = UniformView.asList(object);
			return new ListTreeNode(displayKey, object, list, context);
		}

		if (object instanceof Iterable) {
			Iterable<?> iterable = (Iterable<?>) object;
			return new IterableBasedObjectContainerTreeNode(displayKey, object, iterable, null, context);
		}
		if (object instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) object;
			Set<?> keySet = map.keySet();
			return new IterableBasedObjectContainerTreeNode(displayKey, object, keySet, map::get, context);
		}
		if (object instanceof Multimap) {
			Multimap<Object, ?> multimap = (Multimap) object;
			Set<?> keySet = multimap.keySet();
			return new IterableBasedObjectContainerTreeNode(displayKey, object, keySet, multimap::get, context);
		}
		return new DefaultObjectTreeNode(displayKey, object, context);
	}
}
