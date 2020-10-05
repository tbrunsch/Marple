package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.TypedObjectInfo;
import dd.kms.marple.impl.common.UniformView;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public class InspectionTreeNodes
{
	public static TreeModel createModel(@Nullable String fieldName, ObjectInfo objectInfo, InspectionContext context) {
		InspectionTreeNode treeNode = create(fieldName, objectInfo, context);
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

	static InspectionTreeNode create(@Nullable String displayKey, ObjectInfo objectInfo, InspectionContext context) {
		if (UniformView.canViewAsList(objectInfo)) {
			TypedObjectInfo<List<?>> listInfo = UniformView.asList(objectInfo);
			List<?> list = listInfo.getObject();
			return new ListTreeNode(displayKey, objectInfo, list, context);
		}

		Object object = objectInfo.getObject();
		if (Iterable.class.isInstance(object)) {
			Iterable<?> iterable = Iterable.class.cast(object);
			return new IterableBasedObjectContainerTreeNode(displayKey, objectInfo, iterable, null, context);
		}
		if (Map.class.isInstance(object)) {
			Map<?, ?> map = Map.class.cast(object);
			Set<?> keySet = map.keySet();
			return new IterableBasedObjectContainerTreeNode(displayKey, objectInfo, keySet, map::get, context);
		}
		if (Multimap.class.isInstance(object)) {
			Multimap multimap = Multimap.class.cast(object);
			Set<?> keySet = multimap.keySet();
			return new IterableBasedObjectContainerTreeNode(displayKey, objectInfo, keySet, multimap::get, context);
		}
		return new DefaultObjectTreeNode(displayKey, objectInfo, context);
	}
}
