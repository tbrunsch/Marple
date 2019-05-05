package dd.kms.marple.gui.inspector.views;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeMouseListener;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeMouseMotionListener;
import dd.kms.marple.gui.actionprovidertree.inspectiontree.InspectionTreeNodes;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;

public class FieldTree extends JPanel
{
	private final JTree			tree			= new JTree();
	private final JScrollPane	treeScrollPane	= new JScrollPane(tree);

	public FieldTree(Object object, InspectionContext inspectionContext) {
		super(new GridBagLayout());

		TreeModel model = InspectionTreeNodes.createModel(null, object, inspectionContext);
		tree.setModel(model);

		add(treeScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		tree.addMouseListener(new ActionProviderTreeMouseListener());
		tree.addMouseMotionListener(new ActionProviderTreeMouseMotionListener());
	}
}