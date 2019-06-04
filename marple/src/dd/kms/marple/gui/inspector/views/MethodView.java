package dd.kms.marple.gui.inspector.views;

import dd.kms.marple.InspectionContext;

import java.awt.*;

public class MethodView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Methods";

	public MethodView(Object object, InspectionContext inspectionContext) {
		super(NAME, object, inspectionContext);
	}

	@Override
	Component createView(ViewType viewType, Object object, InspectionContext inspectionContext) {
		return viewType == ViewType.QUICK
			? new MethodList(object, inspectionContext)
			: new MethodTable(object, inspectionContext);
	}
}
