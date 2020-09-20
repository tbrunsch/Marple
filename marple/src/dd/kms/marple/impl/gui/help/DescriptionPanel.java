package dd.kms.marple.impl.gui.help;

import dd.kms.marple.impl.gui.common.GuiCommons;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;

class DescriptionPanel extends JPanel
{
	private final JLabel	descriptionLabel	= new JLabel();
	private final JLabel	infoLabel			= new JLabel();

	DescriptionPanel(String description, String info) {
		super(new GridBagLayout());

		descriptionLabel.setText(description);
		infoLabel.setText(info);

		GuiCommons.setFontStyle(descriptionLabel,	Font.PLAIN);
		GuiCommons.setFontStyle(infoLabel,			Font.PLAIN);

		add(descriptionLabel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, NONE, new Insets(0, 0, 3, 0), 0, 0));
		add(infoLabel,			new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, WEST, NONE, new Insets(3, 0, 0, 0), 0, 0));
	}
}
