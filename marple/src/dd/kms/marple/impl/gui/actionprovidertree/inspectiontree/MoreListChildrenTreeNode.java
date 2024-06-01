package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.actions.ActionProvider;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;

class MoreListChildrenTreeNode extends MoreChildrenTreeNode
{
	private final List<?>				list;
	private final int					start;
	private final int					end;
	private final InspectionContext		context;

	MoreListChildrenTreeNode(List<?> list, int start, int end, InspectionContext context) {
		this.list = list;
		this.start = start;
		this.end = end;
		this.context = context;
	}

	@Override
	public ActionProvider getActionProvider(JTree tree, MouseEvent e) {
		if (InspectionTreeNodes.canDisplayAllSubListElements(start, end)) {
			return null;
		}

		TreeMouseEvent treeMouseEvent = new TreeMouseEvent(tree, e);

		InspectionAction showNextElements = new InspectionActionImpl("Show next elements", () -> showElementsAt(start, treeMouseEvent));
		InspectionAction showMiddleElements = new InspectionActionImpl("Show element in the middle", () -> showElementsAt((start + end)/2, treeMouseEvent));
		InspectionAction showLastElements = new InspectionActionImpl("Show last elements", () -> showElementsAt(end - 1, treeMouseEvent));
		ImmutableList<InspectionAction> actions = ImmutableList.of(showNextElements, showMiddleElements, showLastElements);

		return ActionProvider.of("Extend view of list", actions, () -> showNextElements);
	}

	@Override
	public List<InspectionTreeNode> getHiddenChildren() {
		return InspectionTreeNodes.getListElementNodes(list, start, end, start, context);
	}

	private void showElementsAt(int index, TreeMouseEvent e) {
		List<InspectionTreeNode> children = InspectionTreeNodes.getListElementNodes(list, start, end, index, context);
		InspectionTreeNodes.replaceNode(e.getParentPath(), e.getParentNode(), this, children, e.getTreeModel());
	}

	private static class InspectionActionImpl implements InspectionAction
	{
		private final String	name;
		private final Runnable	runnable;

		private InspectionActionImpl(String name, Runnable runnable) {
			this.name = name;
			this.runnable = runnable;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public void perform() {
			runnable.run();
		}
	}
}
