package dd.kms.marple.impl.actions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.marple.impl.gui.debugsupport.DebugSupportPanel;

public class DebugSupportAction implements InspectionAction
{
	private static final String	FRAME_TITLE	= "Debug Support";

	private final InspectionContext context;
	private final Object			thisValue;

	public DebugSupportAction(InspectionContext context, Object thisValue) {
		this.context = context;
		this.thisValue = thisValue;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return "Debug support";
	}

	@Override
	public String getDescription() {
		return "Opens the debug support dialog";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		WindowManager.showInFrame(FRAME_TITLE, () -> new DebugSupportPanel(context), panel -> panel.setThisValue(thisValue), panel -> panel.updateContent());
	}
}
