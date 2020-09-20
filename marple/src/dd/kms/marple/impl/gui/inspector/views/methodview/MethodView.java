package dd.kms.marple.impl.gui.inspector.views.methodview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.AbstractQuickAndDetailedView;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.awt.*;

public class MethodView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Methods";

	public MethodView(ObjectInfo objectInfo, InspectionContext context) {
		super(NAME, objectInfo, context);
	}

	@Override
	protected Component createView(ViewType viewType, ObjectInfo objectInfo, InspectionContext context) {
		return viewType == ViewType.QUICK
			? new MethodList(objectInfo, context)
			: new MethodTable(objectInfo, context);
	}
}
