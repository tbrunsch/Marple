package dd.kms.marple.impl.gui.inspector.views.fieldview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.gui.inspector.views.AbstractQuickAndDetailedView;

public class FieldView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Fields";

	public FieldView(Object object, InspectionContext context) {
		super(NAME, object, context);
	}

	@Override
	protected ObjectView createView(ViewType viewType, Object object, InspectionContext context) {
		return viewType == ViewType.QUICK
			? new FieldTree(object, context)
			: new FieldTable(object, context);
	}
}
