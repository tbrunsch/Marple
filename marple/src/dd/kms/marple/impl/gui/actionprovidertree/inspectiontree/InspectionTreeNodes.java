package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.UniformView;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class InspectionTreeNodes
{
	private static final int	MAX_ELEMENTS_TO_DISPLAY	= 100;

	public static TreeModel createModel(@Nullable String fieldName, Object object, InspectionContext context) {
		InspectionTreeNode treeNode = create(fieldName, object, context);
		return new InspectionTreeModel(treeNode);
	}

	public static void enableMoreChildrenNodeReplacement(JTree tree) {
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				TreeMouseEvent treeMouseEvent = new TreeMouseEvent(tree, e);
				InspectionTreeNode node = treeMouseEvent.getNode();
				if (node != null) {
					if (e.isPopupTrigger()) {
						node.handlePopupRequested(treeMouseEvent);
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				TreeMouseEvent treeMouseEvent = new TreeMouseEvent(tree, e);
				InspectionTreeNode node = treeMouseEvent.getNode();
				if (node != null) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						node.handleLeftMouseButtonClicked(treeMouseEvent);
					} else if (e.isPopupTrigger()) {
						node.handlePopupRequested(treeMouseEvent);
					}
				}
			}
		});
	}

	static void replaceNode(TreePath parentPath, InspectionTreeNode parent, InspectionTreeNode node, List<InspectionTreeNode> newNodes, InspectionTreeModel treeModel) {
		int childIndex = parent.getChildIndex(node);
		List<InspectionTreeNode> children = parent.getChildren();
		children.remove(node);
		treeModel.fireTreeNodesRemoved(parentPath.getPath(), new int[]{ childIndex }, new Object[]{ node });
		children.addAll(childIndex, newNodes);
		treeModel.fireTreeNodesInserted(parentPath.getPath(), IntStream.range(childIndex, childIndex + newNodes.size()).toArray(), newNodes.toArray());

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
			builder.add(new MoreIterableChildrenTreeNode(childIterator, maxNumVisibleChildren));
		}
		return builder.build();
	}

	static List<InspectionTreeNode> getListElementNodes(List<?> list, int start, int end, int indexToShow, InspectionContext context) {
		assert start <= indexToShow && indexToShow < end;
		int displayStart = start;
		int displayEnd = end;
		if (!canDisplayAllSubListElements(displayStart, displayEnd)) {
			int preferredEnd = indexToShow + MAX_ELEMENTS_TO_DISPLAY/2;
			int preferredStart = preferredEnd - MAX_ELEMENTS_TO_DISPLAY;
			if (preferredStart < displayStart) {
				displayEnd = displayStart + MAX_ELEMENTS_TO_DISPLAY;
			} else if (preferredEnd > displayEnd) {
				displayStart = displayEnd - MAX_ELEMENTS_TO_DISPLAY;
			} else {
				displayStart = preferredStart;
				displayEnd = preferredEnd;
			}
		}
		ImmutableList.Builder<InspectionTreeNode> builder = ImmutableList.builder();
		if (displayStart > start) {
			InspectionTreeNode moreBeforeNode = new MoreListChildrenTreeNode(list, start, displayStart, context);
			builder.add(moreBeforeNode);
		}
		for (int index = displayStart; index < displayEnd; index++) {
			Object element = list.get(index);
			InspectionTreeNode elementNode = InspectionTreeNodes.create("[" + index + "]", element, context);
			builder.add(elementNode);
		}
		if (displayEnd < end) {
			InspectionTreeNode moreAfterNode = new MoreListChildrenTreeNode(list, displayEnd, end, context);
			builder.add(moreAfterNode);
		}
		return builder.build();
	}

	static boolean canDisplayAllSubListElements(int start, int end) {
		return end - start <= MAX_ELEMENTS_TO_DISPLAY;
	}

	static InspectionTreeNode create(@Nullable String displayKey, Object object, InspectionContext context) {
		if (UniformView.canViewAsList(object)) {
			List<?> list = UniformView.asList(object);
			//return new ListTreeNode(displayKey, object, list, context);
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
