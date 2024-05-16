package dd.kms.marple.impl.gui.search;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNode;
import dd.kms.marple.impl.instancesearch.InstancePath;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseEvent;

class SearchNode extends DefaultMutableTreeNode implements ActionProviderTreeNode
{
	private final InspectionContext	context;

	SearchNode(InstancePath path, InspectionContext context) {
		super(path);
		this.context = context;
	}

	@Override
	public @Nullable ActionProvider getActionProvider(JTree tree, MouseEvent e) {
		return new ActionProviderBuilder(getFullText(), getObject(), context).build();
	}

	String getFullPathAsString() {
		return getUserObject().toString();
	}

	@Override
	public String getFullText() {
		InstancePath instancePath = getUserObject();
		String lastNodeStringRepresentation = instancePath.getLastNodeStringRepresentation();
		int beginIndex = lastNodeStringRepresentation.startsWith(".") ? 1 : 0;
		return lastNodeStringRepresentation.substring(beginIndex);
	}

	public Object getObject() {
		return getUserObject().getLastNodeObject();
	}

	@Override
	public InstancePath getUserObject() {
		return (InstancePath) super.getUserObject();
	}

	@Override
	public String toString() {
		return getTrimmedText();
	}
}
