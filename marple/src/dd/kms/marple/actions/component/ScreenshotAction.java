package dd.kms.marple.actions.component;

import com.google.common.util.concurrent.Runnables;
import dd.kms.marple.actions.ImmediateInspectionAction;
import dd.kms.marple.gui.common.WindowManager;

import javax.swing.*;
import java.awt.*;

public class ScreenshotAction implements ImmediateInspectionAction
{
	private static final String	FRAME_TITLE	= "Screenshot";

	private final JComponent	component;

	public ScreenshotAction(JComponent component) {
		this.component = component;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return "Screenshot";
	}

	@Override
	public String getDescription() {
		return "Take a screenshot of the component";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		WindowManager.showInFrame(FRAME_TITLE, ScreenshotPanel::new, panel -> panel.takeScreenshot(component), panel -> {});
	}

	@Override
	public void performImmediately() {
		JFrame window = WindowManager.getWindow(FRAME_TITLE, () -> null, Runnables.doNothing());
		WindowManager.configureComponent(window, this::takeLiveScreenshot);
	}

	private void takeLiveScreenshot(ScreenshotPanel screenshotPanel) {
		screenshotPanel.takeLiveScreenshot(component);
	}
}
