package dd.kms.marple.actions.component;

import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.gui.common.Screenshots;
import dd.kms.marple.gui.common.WindowManager;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class ScreenshotAction implements InspectionAction
{
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
		BufferedImage screenshot = Screenshots.takeScreenshot(component);
		WindowManager.showInFrame("Screenshot", ScreenshotPanel::new, panel -> panel.setScreenshot(screenshot), panel -> {});
	}
}
