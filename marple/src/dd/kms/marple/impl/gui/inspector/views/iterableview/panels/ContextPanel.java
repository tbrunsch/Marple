package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.TypedObjectInfo;
import dd.kms.marple.impl.gui.inspector.views.fieldview.FieldTree;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class ContextPanel extends JPanel
{
	private final JLabel		commonElementClassInfoLabel	= new JLabel("Element class:");
	private final JLabel		commonElementClassLabel		= new JLabel();

	private final JComponent	fieldTree;

	public ContextPanel(TypedObjectInfo<? extends Iterable<?>> iterableInfo, TypeInfo commonElementType, InspectionContext context) {
		super(new GridBagLayout());

		setBorder(BorderFactory.createTitledBorder("Iterable"));

		fieldTree = new FieldTree(iterableInfo, context);
		fieldTree.setPreferredSize(new Dimension(fieldTree.getPreferredSize().width, 100));

		commonElementClassLabel.setText(commonElementType.toString());

		add(commonElementClassInfoLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(commonElementClassLabel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		add(fieldTree, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
	}
}
