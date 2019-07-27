package dd.kms.marple.actions.component;

import com.google.common.util.concurrent.Runnables;
import dd.kms.marple.actions.ImmediateInspectionAction;
import dd.kms.marple.gui.common.WindowManager;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class SnapshotAction<T> implements ImmediateInspectionAction
{
	private static final String	FRAME_TITLE	= "Snapshot";

	private final T 							snapshotTarget;
	private final Function<T, BufferedImage>	snapshotFunction;

	public SnapshotAction(T snapshotTarget, Function<T, BufferedImage> snapshotFunction) {
		this.snapshotTarget = snapshotTarget;
		this.snapshotFunction = snapshotFunction;
	}

	@Override
	public final boolean isDefaultAction() {
		return false;
	}

	@Override
	public final String getName() {
		return "Snapshot";
	}

	@Override
	public String getDescription() {
		return "Take a snapshot of the component, image, icon, or color";
	}

	@Override
	public final boolean isEnabled() {
		return true;
	}

	@Override
	public final void perform() {
		WindowManager.showInFrame(FRAME_TITLE, SnapshotPanel::new, panel -> panel.takeSnapshot(snapshotTarget, snapshotFunction), panel -> {});
	}

	@Override
	public final void performImmediately() {
		JFrame window = WindowManager.getWindow(FRAME_TITLE, () -> null, Runnables.doNothing());
		WindowManager.<SnapshotPanel>configureComponent(window, panel -> panel.takeLiveSnapshot(snapshotTarget, snapshotFunction));
	}
}
