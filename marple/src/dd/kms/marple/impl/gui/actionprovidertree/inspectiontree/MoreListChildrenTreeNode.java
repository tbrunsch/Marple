package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.Actions;

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
	public List<InspectionTreeNode> getHiddenChildren() {
		return InspectionTreeNodes.getListElementNodes(list, start, end, start, context);
	}

	@Override
	public void handlePopupRequested(TreeMouseEvent e) {
		MouseEvent mouseEvent = e.getMouseEvent();
		if (!InspectionTreeNodes.canDisplayAllSubListElements(start, end)) {
			ActionProvider actionProvider = createReplaceNodeActionProvider(e);
			Actions.showActionPopup(actionProvider, mouseEvent);
		}
	}

	ActionProvider createReplaceNodeActionProvider(TreeMouseEvent e) {
		if (InspectionTreeNodes.canDisplayAllSubListElements(start, end)) {
			return null;
		}

		InspectionAction showNextElements = new InspectionActionImpl("Show next elements", () -> showElementsAt(start, e), true);
		InspectionAction showMiddleElements = new InspectionActionImpl("Show element in the middle", () -> showElementsAt((start + end)/2, e), false);
		InspectionAction showLastElements = new InspectionActionImpl("Show last elements", () -> showElementsAt(end - 1, e), false);
		ImmutableList<InspectionAction> actions = ImmutableList.of(showNextElements, showMiddleElements, showLastElements);

		return ActionProvider.of("Extend view of list", actions, true);
	}

	private void showElementsAt(int index, TreeMouseEvent e) {
		List<InspectionTreeNode> children = InspectionTreeNodes.getListElementNodes(list, start, end, index, context);
		InspectionTreeNodes.replaceNode(e.getParentPath(), e.getParentNode(), this, children, e.getTreeModel());
	}

	private static class InspectionActionImpl implements InspectionAction
	{
		private final String	name;
		private final Runnable	runnable;
		private final boolean	defaultAction;

		private InspectionActionImpl(String name, Runnable runnable, boolean defaultAction) {
			this.name = name;
			this.runnable = runnable;
			this.defaultAction = defaultAction;
		}

		@Override
		public boolean isDefaultAction() {
			return defaultAction;
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
