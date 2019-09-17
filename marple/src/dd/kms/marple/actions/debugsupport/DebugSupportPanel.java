package dd.kms.marple.actions.debugsupport;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.common.CurrentObjectPanel;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

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

	DebugSupportPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		currentObjectPanel = new CurrentObjectPanel(inspectionContext);
		unnamedSlotsPanel = new UnnamedSlotsPanel(inspectionContext);
		namedSlotsPanel = new NamedSlotsPanel(inspectionContext);
		breakpointTriggerPanel = new BreakpointTriggerPanel(inspectionContext);

		int yPos = 0;
		add(currentObjectPanel,		new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(unnamedSlotsPanel,		new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.3, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(namedSlotsPanel,		new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.3, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(breakpointTriggerPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.1, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		setPreferredSize(new Dimension(640, 640));
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
}
