package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.common.UniformView;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
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
		if (parent == null) {
			// replace root
			Preconditions.checkArgument(newNodes.size() == 1, "The root node must be replaced by a single node");
			InspectionTreeNode newRoot = newNodes.get(0);
			treeModel.setRoot(newRoot);
			treeModel.fireTreeStructureChanged();
			return;
		}

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
		if (end <= start) {
			return ImmutableList.of();
		}
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
		return create(displayKey, object, context, ViewOption.DEFAULT);
	}

	static InspectionTreeNode create(@Nullable String displayKey, Object object, InspectionContext context, ViewOption viewOption) {
		List<ViewOption> supportedViews = getSupportedViews(object);

		for (ViewOption view : supportedViews) {
			if (viewOption != ViewOption.DEFAULT && viewOption != view) {
				continue;
			}
			List<ViewOption> alternativeViews = new ArrayList<>(supportedViews);
			alternativeViews.remove(view);

			switch (view) {
				case AS_LIST: {
					List<?> list = UniformView.asList(object);
					return new ListTreeNode(displayKey, object, list, context, alternativeViews);
				}
				case AS_ITERABLE: {
					Iterable<?> iterable = (Iterable<?>) object;
					return new IterableBasedObjectContainerTreeNode(displayKey, object, iterable, null, context, alternativeViews);
				}
				case AS_MAP: {
					Map<?, ?> map = (Map<?, ?>) object;
					Set<?> keySet = map.keySet();
					return new IterableBasedObjectContainerTreeNode(displayKey, object, keySet, map::get, context, alternativeViews);
				}
				case AS_MULTIMAP: {
					Multimap<Object, ?> multimap = (Multimap) object;
					Set<?> keySet = multimap.keySet();
					return new IterableBasedObjectContainerTreeNode(displayKey, object, keySet, multimap::get, context, alternativeViews);
				}
				case AS_OBJECT:
					return new DefaultObjectTreeNode(displayKey, object, context, alternativeViews);
				default:
					throw new UnsupportedOperationException("Unsupported view: " + view);
			}
		}
		throw new IllegalStateException("No view available for this object");
	}

	private static List<ViewOption> getSupportedViews(Object object) {
		ImmutableList.Builder<ViewOption> builder = ImmutableList.builder();
		boolean hasListView = UniformView.canViewAsList(object);
		if (hasListView) {
			builder.add(ViewOption.AS_LIST);
		}
		if (!hasListView && object instanceof Iterable) {
			builder.add(ViewOption.AS_ITERABLE);
		}
		if (object instanceof Map) {
			builder.add(ViewOption.AS_MAP);
		}
		if (object instanceof Multimap) {
			builder.add(ViewOption.AS_MULTIMAP);
		}
		builder.add(ViewOption.AS_OBJECT);
		return builder.build();
	}

	static void addAlternativeViewActions(ActionProviderBuilder actionProviderBuilder, AbstractInspectionTreeNode node, List<ViewOption> alternativeViews, JTree tree, MouseEvent e) {
		TreeMouseEvent treeMouseEvent = new TreeMouseEvent(tree, e);
		for (ViewOption alternativeView : alternativeViews) {
			actionProviderBuilder.addAdditionalAction(new ChangeNodeViewAction(node, alternativeView, treeMouseEvent));
		}
	}
}
