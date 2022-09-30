package dd.kms.marple.impl.gui.inspector.views.mapview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.marple.impl.gui.inspector.views.fieldview.FieldTree;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class ContextPanel extends JPanel
{
	private final JLabel		commonKeyClassInfoLabel		= new JLabel("Key class:");
	private final JLabel		commonKeyClassLabel			= new JLabel();
	private final JLabel		commonValueClassInfoLabel	= new JLabel("Value class:");
	private final JLabel		commonValueClassLabel		= new JLabel();

	private final JComponent	fieldTree;

	public ContextPanel(Map<?, ?> map, Class<?> commonKeyType, Class<?> commonValueType, InspectionContext context) {
		super(new GridBagLayout());

		setBorder(BorderFactory.createTitledBorder("Map"));

		fieldTree = new FieldTree(map, context);
		fieldTree.setPreferredSize(new Dimension(fieldTree.getPreferredSize().width, 100));

		commonKeyClassLabel.setText(context.getDisplayText(commonKeyType));
		commonValueClassLabel.setText(context.getDisplayText(commonValueType));

		int yPos = 0;
		int xPos = 0;
		add(commonKeyClassInfoLabel,	new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(commonKeyClassLabel,		new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(commonValueClassInfoLabel,	new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(commonValueClassLabel,		new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(fieldTree, new GridBagConstraints(xPos++, yPos++, 2, 1, 1.0, 1.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
	}
}
