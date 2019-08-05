package dd.kms.marple.actions.debugsupport;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.gui.common.WindowManager;

public class DebugSupportAction implements InspectionAction
{
	private static final String	FRAME_TITLE	= "Debug Support";

	private final InspectionContext inspectionContext;
	private final Object			thisValue;

	public DebugSupportAction(InspectionContext inspectionContext, Object thisValue) {
		this.inspectionContext = inspectionContext;
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
		WindowManager.showInFrame(FRAME_TITLE, () -> new DebugSupportPanel(inspectionContext), panel -> panel.setThisValue(thisValue), panel -> panel.updateContent());
	}
}
