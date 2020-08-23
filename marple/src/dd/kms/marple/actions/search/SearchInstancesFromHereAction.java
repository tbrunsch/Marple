package dd.kms.marple.actions.search;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.search.InstanceSearchFrame;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public class SearchInstancesFromHereAction extends AbstractInstanceSearchAction
{
	public SearchInstancesFromHereAction(InspectionContext inspectionContext, ObjectInfo thisValue) {
		super(inspectionContext, thisValue);
	}

	@Override
	void configureSearchFrame(InstanceSearchFrame searchFrame, ObjectInfo thisValue) {
		searchFrame.setRoot(thisValue.getObject());
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
