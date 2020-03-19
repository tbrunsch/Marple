package dd.kms.marple.actions.debugsupport;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.common.CurrentObjectPanel;
import dd.kms.marple.gui.common.ExceptionFormatter;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

class DebugSupportPanel extends JPanel
{
	private final CurrentObjectPanel		currentObjectPanel;
	private final UnnamedSlotsPanel			unnamedSlotsPanel;
	private final NamedSlotsPanel 			namedSlotsPanel;
	private final BreakpointTriggerPanel	breakpointTriggerPanel;
	private final JPanel					exceptionPanel			= new JPanel(new GridBagLayout());
	private final JLabel					exceptionLabel			= new JLabel();

	DebugSupportPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		currentObjectPanel = new CurrentObjectPanel(inspectionContext);
		unnamedSlotsPanel = new UnnamedSlotsPanel(this::onException, inspectionContext);
		namedSlotsPanel = new NamedSlotsPanel(this::onException, inspectionContext);
		breakpointTriggerPanel = new BreakpointTriggerPanel(this::onException, inspectionContext);

		int yPos = 0;
		add(currentObjectPanel,		new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(unnamedSlotsPanel,		new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.3, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(namedSlotsPanel,		new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.3, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(breakpointTriggerPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.1, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(exceptionPanel,			new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.1, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		exceptionPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		exceptionPanel.setVisible(false);
		exceptionPanel.add(exceptionLabel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		exceptionLabel.setForeground(Color.RED);

		setPreferredSize(new Dimension(640, 800));
	}

	void setThisValue(ObjectInfo thisValue) {
		currentObjectPanel.setCurrentObject(thisValue);
		unnamedSlotsPanel.setThisValue(thisValue);
		namedSlotsPanel.setThisValue(thisValue);
		breakpointTriggerPanel.setThisValue(thisValue);
	}

	void updateContent() {
		unnamedSlotsPanel.updateContent();
		namedSlotsPanel.updateContent();
	}

	private void onException(@Nullable Throwable exception) {
		exceptionPanel.setVisible(exception != null);
		String exceptionMessage = exception == null ? null : ExceptionFormatter.formatException(exception, true);
		exceptionLabel.setText(exceptionMessage);
	}
}
