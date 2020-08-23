package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class GeneralSettingsPanel extends JPanel
{
	private final JLabel						minimumAccessModifierLabel		= new JLabel("Minimum access modifier:");
	private final MinimumAccessModifierControls	minimumAccessModifierControls;

	private final JLabel						dynamicTypingLabel				= new JLabel("Dynamic typing:");
	private final DynamicTypingControls			dynamicTypingControls;

	public GeneralSettingsPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		minimumAccessModifierControls = new MinimumAccessModifierControls(inspectionContext);
		dynamicTypingControls = new DynamicTypingControls(inspectionContext);

		add(minimumAccessModifierLabel,									new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		add(minimumAccessModifierControls.getInput(),						new GridBagConstraints(1, 0, REMAINDER, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		add(dynamicTypingLabel,											new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		add(dynamicTypingControls.getDynamicTypingOffRadioButton(),		new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		add(dynamicTypingControls.getDynamicTypingOnRadioButton(),		new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));

		add(dynamicTypingControls.getDynamicTypingInfo(),				new GridBagConstraints(0, 2, REMAINDER, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		minimumAccessModifierControls.updateControls();
		dynamicTypingControls.updateControls();
	}

	public void updateContent() {
		minimumAccessModifierControls.updateControls();
		dynamicTypingControls.updateControls();
	}
}
