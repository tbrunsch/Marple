package dd.kms.marple.impl.gui.debugsupport;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.gui.common.CurrentObjectPanel;
import dd.kms.marple.impl.gui.common.ExceptionFormatter;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

public class DebugSupportPanel extends JPanel
{
	private final CurrentObjectPanel		currentObjectPanel;
	private final UnnamedSlotsPanel			unnamedSlotsPanel;
	private final NamedSlotsPanel 			namedSlotsPanel;
	private final BreakpointTriggerPanel	breakpointTriggerPanel;
	private final JPanel					exceptionPanel			= new JPanel(new GridBagLayout());
	private final JLabel					exceptionLabel			= new JLabel();

	public DebugSupportPanel(InspectionContext context) {
		super(new GridBagLayout());

		currentObjectPanel = new CurrentObjectPanel(context);
		unnamedSlotsPanel = new UnnamedSlotsPanel(this::onException, context);
		namedSlotsPanel = new NamedSlotsPanel(this::onException, context);
		breakpointTriggerPanel = new BreakpointTriggerPanel(this::onException, context);

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

	public void setThisValue(ObjectInfo thisValue) {
		ObjectInfo runtimeInfo = ReflectionUtils.getRuntimeInfo(thisValue);
		currentObjectPanel.setCurrentObject(runtimeInfo);
		unnamedSlotsPanel.setThisValue(runtimeInfo);
		namedSlotsPanel.setThisValue(runtimeInfo);
		breakpointTriggerPanel.setThisValue(runtimeInfo);
	}

	public void updateContent() {
		unnamedSlotsPanel.updateContent();
		namedSlotsPanel.updateContent();
	}

	private void onException(@Nullable Throwable exception) {
		exceptionPanel.setVisible(exception != null);
		String exceptionMessage = exception == null ? null : ExceptionFormatter.formatException(exception, true);
		exceptionLabel.setText(exceptionMessage);
	}
}
