package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import dd.kms.marple.impl.actions.ActionProvider;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

abstract class MoreChildrenTreeNode implements InspectionTreeNode
{
	abstract List<InspectionTreeNode> getHiddenChildren();

	@Override
	public ActionProvider getActionProvider(JTree tree, MouseEvent e) {
		TreeMouseEvent treeMouseEvent = new TreeMouseEvent(tree, e);
		ShowNextElementsAction showNextElementsAction = new ShowNextElementsAction(this, treeMouseEvent);
		return ActionProvider.of("", Collections.singletonList(showNextElementsAction), showNextElementsAction);
	}

	@Override
	public int getChildIndex(Object child) {
		return -1;
	}

	@Override
	public List<InspectionTreeNode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public String getFullText() {
		return "more ...";
	}

	@Override
	public final String toString() {
		return getTrimmedText();
	}
}
