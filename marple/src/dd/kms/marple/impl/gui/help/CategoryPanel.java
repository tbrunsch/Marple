package dd.kms.marple.impl.gui.help;

import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.impl.gui.common.GuiCommons;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;

class CategoryPanel extends JPanel
{
	private final JLabel	titleLabel			= new JLabel();
	private final JLabel	keyDescriptionLabel	= new JLabel();

	CategoryPanel(String title, KeyRepresentation key) {
		super(new GridBagLayout());

		titleLabel.setText(title);
		keyDescriptionLabel.setText(key.toString());

		setBorder(BorderFactory.createEtchedBorder());

		GuiCommons.setFontStyle(titleLabel,				Font.BOLD);
		GuiCommons.setFontStyle(keyDescriptionLabel,	Font.PLAIN);

		add(titleLabel,				new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(keyDescriptionLabel,	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
	}
}
