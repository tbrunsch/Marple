package dd.kms.marple.impl.actions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.marple.impl.gui.snapshot.SnapshotPanel;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class SnapshotAction<T> implements ImmediateInspectionAction
{
	private static final String	FRAME_TITLE	= "Snapshot";

	private final T 							snapshotTarget;
	private final Function<T, BufferedImage>	snapshotFunction;
	private final InspectionContext				context;

	public SnapshotAction(T snapshotTarget, Function<T, BufferedImage> snapshotFunction, InspectionContext context) {
		this.snapshotTarget = snapshotTarget;
		this.snapshotFunction = snapshotFunction;
		this.context = context;
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
		WindowManager.showInFrame(FRAME_TITLE, () -> new SnapshotPanel(context), panel -> panel.takeSnapshot(snapshotTarget, snapshotFunction), panel -> {});
	}

	@Override
	public final void performImmediately() {
		JFrame window = WindowManager.getWindow(FRAME_TITLE, () -> null);
		WindowManager.<SnapshotPanel>configureComponent(window, panel -> panel.takeLiveSnapshot(snapshotTarget, snapshotFunction));
	}
}
