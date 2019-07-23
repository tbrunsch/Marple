package dd.kms.marple.actions.component;

import com.google.common.util.concurrent.Runnables;
import dd.kms.marple.actions.ImmediateInspectionAction;
import dd.kms.marple.gui.common.WindowManager;

import javax.swing.*;

abstract class AbstractScreenshotAction<T> implements ImmediateInspectionAction
{
	private static final String	FRAME_TITLE	= "Screenshot";

	private final T	screenshotTarget;

	AbstractScreenshotAction(T screenshotTarget) {
		this.screenshotTarget = screenshotTarget;
	}

	abstract void takeScreenshot(ScreenshotPanel panel, T screenshotTarget);
	abstract void takeLiveScreenshot(ScreenshotPanel panel, T screenshotTarget);

	@Override
	public final boolean isDefaultAction() {
		return false;
	}

	@Override
	public final String getName() {
		return "Screenshot";
	}

	@Override
	public final boolean isEnabled() {
		return true;
	}

	@Override
	public final void perform() {
		WindowManager.showInFrame(FRAME_TITLE, ScreenshotPanel::new, panel -> takeScreenshot(panel, screenshotTarget), panel -> {});
	}

	@Override
	public final void performImmediately() {
		JFrame window = WindowManager.getWindow(FRAME_TITLE, () -> null, Runnables.doNothing());
		WindowManager.<ScreenshotPanel>configureComponent(window, panel -> takeLiveScreenshot(panel, screenshotTarget));
	}
}
