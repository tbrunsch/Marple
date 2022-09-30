package dd.kms.marple.impl.actions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.search.InstanceSearchFrame;

public class SearchInstancesFromHereAction extends AbstractInstanceSearchAction
{
	public SearchInstancesFromHereAction(InspectionContext context, Object thisValue) {
		super(context, thisValue);
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
