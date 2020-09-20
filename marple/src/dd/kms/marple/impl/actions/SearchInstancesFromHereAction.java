package dd.kms.marple.impl.actions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.search.InstanceSearchFrame;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public class SearchInstancesFromHereAction extends AbstractInstanceSearchAction
{
	public SearchInstancesFromHereAction(InspectionContext context, ObjectInfo thisValue) {
		super(context, thisValue);
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
