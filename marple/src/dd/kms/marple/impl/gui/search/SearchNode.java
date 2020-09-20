package dd.kms.marple.impl.gui.search;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNode;
import dd.kms.marple.impl.instancesearch.InstancePath;
import dd.kms.zenodot.api.wrappers.InfoProvider;

import javax.annotation.Nullable;
import javax.swing.tree.DefaultMutableTreeNode;

class SearchNode extends DefaultMutableTreeNode implements ActionProviderTreeNode
{
	private final InspectionContext	context;

	SearchNode(InstancePath path, InspectionContext context) {
		super(path);
		this.context = context;
	}

	@Override
	public @Nullable ActionProvider getActionProvider() {
		return new ActionProviderBuilder(getDisplayText(), InfoProvider.createObjectInfo(getObject()), context).build();
	}

	String getFullPathAsString() {
		return getInstancePath().toString();
	}

	private String getDisplayText() {
		InstancePath instancePath = getInstancePath();
		String lastNodeStringRepresentation = instancePath.getLastNodeStringRepresentation();
		int beginIndex = lastNodeStringRepresentation.startsWith(".") ? 1 : 0;
		return lastNodeStringRepresentation.substring(beginIndex);
	}

	private Object getObject() {
		return getInstancePath().getLastNodeObject();
	}

	private InstancePath getInstancePath() {
		return (InstancePath) getUserObject();
	}

	@Override
	public String toString() {
		return getDisplayText();
	}
}
