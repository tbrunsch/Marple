package dd.kms.marple.impl.actions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.marple.impl.gui.search.InstanceSearchFrame;

import java.awt.*;

abstract class AbstractInstanceSearchAction implements InspectionAction
{
	private final InspectionContext	context;
	private final Object			thisValue;

	public AbstractInstanceSearchAction(InspectionContext context, Object thisValue) {
		this.context = context;
		this.thisValue = thisValue;
	}

	abstract void configureSearchFrame(InstanceSearchFrame searchFrame, Object thisValue);

	@Override
	public boolean isEnabled() {
		return context.getEvaluator() != null;
	}

	@Override
	public void perform() {
		showInstanceSearchFrame();
	}

	private void showInstanceSearchFrame() {
		InstanceSearchFrame searchFrame = WindowManager.getWindow(InstanceSearchFrame.class, this::createInstanceSearchFrame);
		configureSearchFrame(searchFrame, thisValue);
	}

	private InstanceSearchFrame createInstanceSearchFrame() {
		InstanceSearchFrame searchFrame = new InstanceSearchFrame(context);
		searchFrame.setPreferredSize(new Dimension(600, 600));
		return searchFrame;
	}
}
