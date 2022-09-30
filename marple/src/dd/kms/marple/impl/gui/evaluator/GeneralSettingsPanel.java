package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class GeneralSettingsPanel extends JPanel
{
	private final JLabel						minimumAccessModifierLabel		= new JLabel("Minimum access modifier:");
	private final MinimumAccessModifierControls	minimumAccessModifierControls;

	private final JLabel						evaluationModeLabel = new JLabel("Evaluation mode:");
	private final EvaluationModePanel			evaluationModePanel;

	public GeneralSettingsPanel(InspectionContext context) {
		super(new GridBagLayout());

		minimumAccessModifierControls = new MinimumAccessModifierControls(context);
		evaluationModePanel = new EvaluationModePanel(context, EvaluationModePanel.Alignment.VERTICAL);

		add(minimumAccessModifierLabel,					new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		add(minimumAccessModifierControls.getInput(),	new GridBagConstraints(1, 0, REMAINDER, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		add(evaluationModeLabel,						new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		add(evaluationModePanel,						new GridBagConstraints(1, 1, REMAINDER, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		add(new JLabel(),								new GridBagConstraints(0, 3, REMAINDER, 1, 1.0, 1.0, NORTHWEST, BOTH, DEFAULT_INSETS, 0, 0));

		minimumAccessModifierControls.updateControls();
		evaluationModePanel.updateControls();
	}

	public void updateContent() {
		minimumAccessModifierControls.updateControls();
		evaluationModePanel.updateControls();
	}
}
