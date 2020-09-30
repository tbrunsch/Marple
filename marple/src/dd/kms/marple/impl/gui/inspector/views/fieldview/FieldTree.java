package dd.kms.marple.impl.gui.inspector.views.fieldview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNodes;
import dd.kms.marple.impl.gui.actionprovidertree.inspectiontree.InspectionTreeNodes;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;

public class FieldTree extends JPanel
{
	private final JTree			tree			= new JTree();
	private final JScrollPane	treeScrollPane	= new JScrollPane(tree);

	public FieldTree(ObjectInfo objectInfo, InspectionContext context) {
		super(new GridBagLayout());

		TreeModel model = InspectionTreeNodes.createModel(null, objectInfo, context);
		tree.setModel(model);

		add(treeScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));

		ActionProviderListeners.addMouseListeners(tree);
		ActionProviderTreeNodes.enableFullTextToolTips(tree);
		InspectionTreeNodes.enableMoreChildrenNodeReplacement(tree);
	}
}
