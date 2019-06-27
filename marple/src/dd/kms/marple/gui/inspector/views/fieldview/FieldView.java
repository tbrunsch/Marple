package dd.kms.marple.gui.inspector.views.fieldview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.inspector.views.AbstractQuickAndDetailedView;

import java.awt.*;

public class FieldView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Fields";

	public FieldView(Object object, InspectionContext inspectionContext) {
		super(NAME, object, inspectionContext);
	}

	@Override
	protected Component createView(ViewType viewType, Object object, InspectionContext inspectionContext) {
		return viewType == ViewType.QUICK
			? new FieldTree(object, true, inspectionContext)
			: new FieldTable(object, inspectionContext);
	}
}
