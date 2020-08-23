package dd.kms.marple.gui.inspector.views.methodview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.inspector.views.AbstractQuickAndDetailedView;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.awt.*;

public class MethodView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Methods";

	public MethodView(ObjectInfo objectInfo, InspectionContext inspectionContext) {
		super(NAME, objectInfo, inspectionContext);
	}

	@Override
	protected Component createView(ViewType viewType, ObjectInfo objectInfo, InspectionContext inspectionContext) {
		return viewType == ViewType.QUICK
			? new MethodList(objectInfo, inspectionContext)
			: new MethodTable(objectInfo, inspectionContext);
	}
}
