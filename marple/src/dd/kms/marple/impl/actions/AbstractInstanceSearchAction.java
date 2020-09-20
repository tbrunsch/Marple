package dd.kms.marple.impl.actions;

import com.google.common.util.concurrent.Runnables;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.marple.impl.gui.search.InstanceSearchFrame;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.awt.*;

abstract class AbstractInstanceSearchAction implements InspectionAction
{
	private final InspectionContext	context;
	private final ObjectInfo		thisValue;

	public AbstractInstanceSearchAction(InspectionContext context, ObjectInfo thisValue) {
		this.context = context;
		this.thisValue = thisValue;
	}

	abstract void configureSearchFrame(InstanceSearchFrame searchFrame, ObjectInfo thisValue);

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return context.getEvaluator() != null;
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
		InstanceSearchFrame searchFrame = new InstanceSearchFrame(context);
		searchFrame.setPreferredSize(new Dimension(600, 600));
		return searchFrame;
	}
}
