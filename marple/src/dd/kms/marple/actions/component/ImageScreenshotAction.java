package dd.kms.marple.actions.component;

import java.awt.*;

public class ImageScreenshotAction extends AbstractScreenshotAction<Image>
{
	public ImageScreenshotAction(Image image) {
		super(image);
	}

	@Override
	public String getDescription() {
		return "Display the image";
	}

	@Override
	void takeScreenshot(ScreenshotPanel panel, Image screenshotTarget) {
		panel.takeScreenshot(screenshotTarget);
	}

	@Override
	void takeLiveScreenshot(ScreenshotPanel panel, Image screenshotTarget) {
		panel.takeLiveScreenshot(screenshotTarget);
	}
}
