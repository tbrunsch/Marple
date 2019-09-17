package dd.kms.marple.gui.inspector.views.fieldview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.gui.actionprovidertree.inspectiontree.InspectionTreeNodes;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;

public class FieldTree extends JPanel
{
	private final JTree			tree			= new JTree();
	private final JScrollPane	treeScrollPane	= new JScrollPane(tree);

	public FieldTree(ObjectInfo objectInfo, boolean limitTreeSize, InspectionContext inspectionContext) {
		super(new GridBagLayout());

		TreeModel model = InspectionTreeNodes.createModel(null, objectInfo, limitTreeSize, inspectionContext);
		tree.setModel(model);

		add(treeScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));

		ActionProviderListeners.addMouseListeners(tree);
	}
}
