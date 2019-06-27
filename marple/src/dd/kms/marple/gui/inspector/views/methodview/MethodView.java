package dd.kms.marple.gui.inspector.views.methodview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.inspector.views.AbstractQuickAndDetailedView;

import java.awt.*;

public class MethodView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Methods";

	public MethodView(Object object, InspectionContext inspectionContext) {
		super(NAME, object, inspectionContext);
	}

	@Override
	protected Component createView(ViewType viewType, Object object, InspectionContext inspectionContext) {
		return viewType == ViewType.QUICK
			? new MethodList(object, inspectionContext)
			: new MethodTable(object, inspectionContext);
	}
}
