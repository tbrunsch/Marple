package dd.kms.marple.impl.gui.debugsupport;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.impl.gui.common.CurrentObjectPanel;
import dd.kms.marple.impl.gui.common.ExceptionFormatter;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

public class DebugSupportPanel extends JPanel implements Disposable
{
	private final CurrentObjectPanel		currentObjectPanel;
	private final UnnamedSlotsPanel			unnamedSlotsPanel;
	private final NamedSlotsPanel 			namedSlotsPanel;
	private final JPanel					exceptionPanel			= new JPanel(new GridBagLayout());
	private final JLabel					exceptionLabel			= new JLabel();

	public DebugSupportPanel(InspectionContext context) {
		super(new GridBagLayout());

		currentObjectPanel = new CurrentObjectPanel(context);
		unnamedSlotsPanel = new UnnamedSlotsPanel(this::onException, context);
		namedSlotsPanel = new NamedSlotsPanel(this::onException, context);

		int yPos = 0;
		add(currentObjectPanel,		new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(unnamedSlotsPanel,		new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.3, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(namedSlotsPanel,		new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.3, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(exceptionPanel,			new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.1, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		exceptionPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		exceptionPanel.setVisible(false);
		exceptionPanel.add(exceptionLabel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		exceptionLabel.setForeground(Color.RED);

		setPreferredSize(new Dimension(640, 560));
	}

	public void setThisValue(Object thisValue) {
		currentObjectPanel.setCurrentObject(thisValue);
		unnamedSlotsPanel.setThisValue(thisValue);
		namedSlotsPanel.setThisValue(thisValue);
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

	@Override
	public void dispose() {
		currentObjectPanel.dispose();
		unnamedSlotsPanel.dispose();
		namedSlotsPanel.dispose();
	}
}
