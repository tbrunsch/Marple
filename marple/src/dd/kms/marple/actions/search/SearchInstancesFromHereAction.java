package dd.kms.marple.actions.search;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.search.InstanceSearchFrame;

public class SearchInstancesFromHereAction extends AbstractInstanceSearchAction
{
	public SearchInstancesFromHereAction(InspectionContext inspectionContext, Object thisValue) {
		super(inspectionContext, thisValue);
	}

	@Override
	void configureSearchFrame(InstanceSearchFrame searchFrame, Object thisValue) {
		searchFrame.setRoot(thisValue);
	}

	@Override
	public String getDescription() {
		return "Prepares an instance search starting from here.";
	}

	@Override
	public String getName() {
		return "Search instances from here";
	}
}
