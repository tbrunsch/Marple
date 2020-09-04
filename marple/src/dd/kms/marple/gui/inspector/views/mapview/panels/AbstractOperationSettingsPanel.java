package dd.kms.marple.gui.inspector.views.mapview.panels;

import dd.kms.marple.gui.inspector.views.mapview.settings.OperationSettings;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

abstract class AbstractOperationSettingsPanel extends JPanel
{
	AbstractOperationSettingsPanel() {
		super(new GridBagLayout());
	}

	abstract void setExceptionConsumer(Consumer<Throwable> exceptionConsumer);
	abstract void setAction(Runnable action);
	abstract OperationSettings getSettings();
	abstract void setSettings(OperationSettings settings);
}
