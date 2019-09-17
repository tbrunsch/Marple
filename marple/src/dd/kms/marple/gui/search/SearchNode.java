package dd.kms.marple.gui.search;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeNode;
import dd.kms.marple.instancesearch.InstancePath;
import dd.kms.zenodot.utils.wrappers.InfoProvider;

import javax.annotation.Nullable;
import javax.swing.tree.DefaultMutableTreeNode;

class SearchNode extends DefaultMutableTreeNode implements ActionProviderTreeNode
{
	private final InspectionContext	inspectionContext;

	SearchNode(InstancePath path, InspectionContext inspectionContext) {
		super(path);
		this.inspectionContext = inspectionContext;
	}

	@Override
	public @Nullable ActionProvider getActionProvider() {
		return new ActionProviderBuilder(getDisplayText(), InfoProvider.createObjectInfo(getObject()), inspectionContext).build();
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
