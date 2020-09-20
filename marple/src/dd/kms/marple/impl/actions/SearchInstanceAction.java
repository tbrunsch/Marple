package dd.kms.marple.impl.actions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.search.InstanceSearchFrame;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public class SearchInstanceAction extends AbstractInstanceSearchAction
{
	public SearchInstanceAction(InspectionContext context, ObjectInfo target) {
		super(context, target);
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
