package dd.kms.marple.impl.actions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.search.InstanceSearchFrame;

public class SearchInstanceAction extends AbstractInstanceSearchAction
{
	public SearchInstanceAction(InspectionContext context, Object target) {
		super(context, target);
	}

	@Override
	void configureSearchFrame(InstanceSearchFrame searchFrame, Object thisValue) {
		searchFrame.setTarget(thisValue);
	}

	@Override
	public String getDescription() {
		return "Prepares a search for this instance.";
	}

	@Override
	public String getName() {
		return "Search this instance";
	}
}
