package dd.kms.marple.actions.component;

import javax.swing.*;

public class ComponentScreenshotAction extends AbstractScreenshotAction<JComponent>
{
	public ComponentScreenshotAction(JComponent component) {
		super(component);
	}

	@Override
	public String getDescription() {
		return "Take a screenshot of the component";
	}

	@Override
	void takeScreenshot(ScreenshotPanel panel, JComponent screenshotTarget) {
		panel.takeScreenshot(screenshotTarget);
	}

	@Override
	void takeLiveScreenshot(ScreenshotPanel panel, JComponent screenshotTarget) {
		panel.takeLiveScreenshot(screenshotTarget);
	}
}
