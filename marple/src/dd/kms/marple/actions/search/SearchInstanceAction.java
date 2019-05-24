package dd.kms.marple.actions.search;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.search.InstanceSearchFrame;

public class SearchInstanceAction extends AbstractInstanceSearchAction
{
	public SearchInstanceAction(InspectionContext inspectionContext, Object thisValue) {
		super(inspectionContext, thisValue);
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
