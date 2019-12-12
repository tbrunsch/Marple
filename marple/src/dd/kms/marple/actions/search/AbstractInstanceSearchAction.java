package dd.kms.marple.actions.search;

import com.google.common.util.concurrent.Runnables;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.gui.common.WindowManager;
import dd.kms.marple.gui.search.InstanceSearchFrame;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.awt.*;

abstract class AbstractInstanceSearchAction implements InspectionAction
{
	private final InspectionContext	inspectionContext;
	private final ObjectInfo		thisValue;

	public AbstractInstanceSearchAction(InspectionContext inspectionContext, ObjectInfo thisValue) {
		this.inspectionContext = inspectionContext;
		this.thisValue = thisValue;
	}

	abstract void configureSearchFrame(InstanceSearchFrame searchFrame, ObjectInfo thisValue);

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return inspectionContext.getEvaluator() != null;
	}

	@Override
	public void perform() {
		showInstanceSearchFrame();
	}

	private void showInstanceSearchFrame() {
		InstanceSearchFrame searchFrame = WindowManager.getWindow(InstanceSearchFrame.class, this::createInstanceSearchFrame, Runnables.doNothing());
		configureSearchFrame(searchFrame, thisValue);
	}

	private InstanceSearchFrame createInstanceSearchFrame() {
		InstanceSearchFrame searchFrame = new InstanceSearchFrame(inspectionContext);
		searchFrame.setPreferredSize(new Dimension(600, 600));
		return searchFrame;
	}
}
