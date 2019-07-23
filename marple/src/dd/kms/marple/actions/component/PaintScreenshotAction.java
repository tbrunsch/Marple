package dd.kms.marple.actions.component;

import java.awt.*;

public class PaintScreenshotAction extends AbstractScreenshotAction<Paint>
{
	public PaintScreenshotAction(Paint paint) {
		super(paint);
	}

	@Override
	public String getDescription() {
		return "Display the color or paint";
	}

	@Override
	void takeScreenshot(ScreenshotPanel panel, Paint screenshotTarget) {
		panel.takeScreenshot(screenshotTarget);
	}

	@Override
	void takeLiveScreenshot(ScreenshotPanel panel, Paint screenshotTarget) {
		panel.takeLiveScreenshot(screenshotTarget);
	}
}
