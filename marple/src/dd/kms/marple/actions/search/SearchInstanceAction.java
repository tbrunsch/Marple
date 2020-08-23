package dd.kms.marple.actions.search;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.search.InstanceSearchFrame;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public class SearchInstanceAction extends AbstractInstanceSearchAction
{
	public SearchInstanceAction(InspectionContext inspectionContext, ObjectInfo target) {
		super(inspectionContext, target);
	}

	@Override
	void configureSearchFrame(InstanceSearchFrame searchFrame, ObjectInfo thisValue) {
		searchFrame.setTarget(thisValue.getObject());
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
