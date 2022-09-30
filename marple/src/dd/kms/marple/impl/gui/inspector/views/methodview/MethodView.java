package dd.kms.marple.impl.gui.inspector.views.methodview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.gui.inspector.views.AbstractQuickAndDetailedView;

public class MethodView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Methods";

	public MethodView(Object object, InspectionContext context) {
		super(NAME, object, context);
	}

	@Override
	protected ObjectView createView(ViewType viewType, Object object, InspectionContext context) {
		return viewType == ViewType.QUICK
			? new MethodList(object, context)
			: new MethodTable(object, context);
	}
}
