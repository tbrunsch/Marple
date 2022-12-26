package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.marple.impl.gui.inspector.views.fieldview.FieldTree;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class ContextPanel extends JPanel implements Disposable
{
	private final JLabel		commonElementClassInfoLabel	= new JLabel("Element class:");
	private final JLabel		commonElementClassLabel		= new JLabel();

	private final FieldTree		fieldTree;

	public ContextPanel(Iterable<?> iterable, Class<?> commonElementType, InspectionContext context) {
		super(new GridBagLayout());

		setBorder(BorderFactory.createTitledBorder("Iterable"));

		fieldTree = new FieldTree(iterable, context);
		fieldTree.setPreferredSize(new Dimension(fieldTree.getPreferredSize().width, 100));

		commonElementClassLabel.setText(context.getDisplayText(commonElementType));

		add(commonElementClassInfoLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(commonElementClassLabel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		add(fieldTree, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
	}

	@Override
	public void dispose() {
		fieldTree.dispose();
	}
}
