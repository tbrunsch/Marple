package dd.kms.marple.gui.help;

import dd.kms.marple.gui.common.GuiCommons;
import dd.kms.marple.settings.keys.KeyRepresentation;
import dd.kms.marple.settings.keys.KeySettings;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class EvaluationKeysPanel extends JPanel
{
	private static final String	CODE_COMPLETION_DESCRIPTION			= "Suggest code completions";
	private static final String	SHOW_METHOD_ARGUMENTS_DESCRIPTION	= "Show method argument types";

	private final JLabel	keysLabel	= new JLabel("Keys for expression evaluation:");

	private int	yPos;

	EvaluationKeysPanel(KeySettings keySettings) {
		super(new GridBagLayout());

		GuiCommons.setFontStyle(keysLabel, Font.BOLD);

		add(keysLabel, new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		addKeyDescription(CODE_COMPLETION_DESCRIPTION,			keySettings.getCodeCompletionKey());
		addKeyDescription(SHOW_METHOD_ARGUMENTS_DESCRIPTION,	keySettings.getShowMethodArgumentsKey());
	}

	private void addKeyDescription(String description, KeyRepresentation key) {
		JLabel keyLabel = new JLabel(key.toString() + ":");
		JLabel descriptionLabel = new JLabel(description);

		GuiCommons.setFontStyle(keyLabel, 			Font.PLAIN);
		GuiCommons.setFontStyle(descriptionLabel,	Font.PLAIN);

		add(keyLabel,			new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(descriptionLabel,	new GridBagConstraints(1, yPos++, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
	}
}
