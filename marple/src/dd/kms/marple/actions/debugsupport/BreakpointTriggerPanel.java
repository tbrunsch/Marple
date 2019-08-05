package dd.kms.marple.actions.debugsupport;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.evaluator.textfields.ExpressionInputTextField;
import dd.kms.marple.settings.DebugSettings;
import dd.kms.zenodot.ParseException;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.*;

class BreakpointTriggerPanel extends JPanel
{
	private static final Insets	DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	private final JLabel					methodLabel				= new JLabel("Method:");

	private final JRadioButton				predefinedMethodRB		= new JRadioButton("predefined method:");
	private final JRadioButton				customMethodRB			= new JRadioButton("custom method:");
	private final ButtonGroup				methodButtonGroup		= new ButtonGroup();

	private final JLabel					predefinedMethodLabel	= new JLabel("");
	private final ExpressionInputTextField	customMethodTF;

	private final JLabel					descriptionLabel		= new JLabel("<html><p>Set a breakpoint in the specified method and click the 'Trigger' button</p></html>");
	private final JButton					triggerButton			= new JButton("Trigger");

	private final DebugSettings				debugSettings;

	BreakpointTriggerPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		debugSettings = inspectionContext.getSettings().getDebugSettings();

		this.customMethodTF = new ExpressionInputTextField(inspectionContext);

		setBorder(BorderFactory.createTitledBorder("Breakpoint Trigger"));

		int yPos = 0;

		add(methodLabel,			new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(predefinedMethodRB,		new GridBagConstraints(1, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(predefinedMethodLabel,	new GridBagConstraints(2, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		add(customMethodRB,			new GridBagConstraints(1, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(customMethodTF,			new GridBagConstraints(2, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		add(descriptionLabel,		new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		add(triggerButton,			new GridBagConstraints(2, yPos++, REMAINDER, 1, 0.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));

		String predefinedMethodName = debugSettings.getBreakpointTriggerCommandDescription();
		predefinedMethodLabel.setText(predefinedMethodName);

		methodButtonGroup.add(predefinedMethodRB);
		methodButtonGroup.add(customMethodRB);

		predefinedMethodRB.setSelected(true);

		addListeners();
	}

	private void addListeners() {
		triggerButton.addActionListener(e -> triggerBreakpoint());
	}

	void setThisValue(Object thisValue) {
		customMethodTF.setThisValue(thisValue);
	}

	private void triggerBreakpoint() {
		if (predefinedMethodRB.isSelected()) {
			debugSettings.getBreakpointTriggerCommand().run();
			return;
		}
		try {
			customMethodTF.evaluateText();
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
