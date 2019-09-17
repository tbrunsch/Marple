package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.common.WindowManager;
import dd.kms.marple.gui.evaluator.EvaluationSettingsPane;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_DISTANCE;
import static java.awt.GridBagConstraints.*;

public class EvaluationTextFieldPanel extends JPanel
{
	private final JButton	settingsButton	= new JButton("...");

	private final InspectionContext	context;

	public EvaluationTextFieldPanel(AbstractInputTextField<?> inputTextField, InspectionContext context) {
		super(new GridBagLayout());

		this.context = context;

		add(inputTextField,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, CENTER, HORIZONTAL, new Insets(0, 0, 0, DEFAULT_DISTANCE/2), 0, 0));
		add(settingsButton,	new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, EAST, NONE, new Insets(0, DEFAULT_DISTANCE/2, 0, 0), 0, 0));

		settingsButton.setPreferredSize(new Dimension(settingsButton.getPreferredSize().width, inputTextField.getPreferredSize().height));

		settingsButton.addActionListener(e -> openSettingsDialog());
	}

	private EvaluationSettingsPane createSettingsPane() {
		return new EvaluationSettingsPane(context);
	}

	private void onShowSettings(EvaluationSettingsPane settingsPane) {
		// currently we do nothing at all
	}

	private void openSettingsDialog() {
		WindowManager.showInFrame("Settings", this::createSettingsPane, this::onShowSettings, EvaluationSettingsPane::updateContent);
	}
}
