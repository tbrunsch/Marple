package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.*;

public class GeneralSettingsPanel extends JPanel
{
	private static final Insets	DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	private final JLabel						minimumAccessLevelLabel		= new JLabel("Minimum access level:");
	private final MinimumAccessLevelControls	minimumAccessLevelControls;

	private final JLabel						dynamicTypingLabel			= new JLabel("Dynamic typing:");
	private final DynamicTypingControls			dynamicTypingControls;

	public GeneralSettingsPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		minimumAccessLevelControls = new MinimumAccessLevelControls(inspectionContext);
		dynamicTypingControls = new DynamicTypingControls(inspectionContext);

		add(minimumAccessLevelLabel,									new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		add(minimumAccessLevelControls.getInput(),						new GridBagConstraints(1, 0, REMAINDER, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		add(dynamicTypingLabel,											new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		add(dynamicTypingControls.getDynamicTypingOffRadioButton(),		new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		add(dynamicTypingControls.getDynamicTypingOnRadioButton(),		new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));

		add(dynamicTypingControls.getDynamicTypingInfo(),				new GridBagConstraints(0, 2, REMAINDER, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		minimumAccessLevelControls.updateControls();
		dynamicTypingControls.updateControls();
	}

	public void updateContent() {
		minimumAccessLevelControls.updateControls();
		dynamicTypingControls.updateControls();
	}
}
