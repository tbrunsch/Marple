package dd.kms.marple.impl.gui.help;

import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.impl.gui.common.GuiCommons;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

abstract class AbstractKeysPanel extends JPanel
{
	private final JLabel	keysLabel;

	private int	yPos;

	AbstractKeysPanel(String title) {
		super(new GridBagLayout());

		keysLabel = new JLabel(title + ":");
		GuiCommons.setFontStyle(keysLabel, Font.BOLD);

		add(keysLabel, new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
	}

	void addKeyDescription(String description, KeyRepresentation key) {
		JLabel keyLabel = new JLabel(key.toString() + ":");
		JLabel descriptionLabel = new JLabel(description);

		GuiCommons.setFontStyle(keyLabel, 			Font.PLAIN);
		GuiCommons.setFontStyle(descriptionLabel,	Font.PLAIN);

		add(keyLabel,			new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(descriptionLabel,	new GridBagConstraints(1, yPos++, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
	}
}
