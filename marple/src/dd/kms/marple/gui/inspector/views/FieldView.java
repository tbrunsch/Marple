package dd.kms.marple.gui.inspector.views;

import dd.kms.marple.InspectionContext;

import java.awt.*;

public class FieldView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Fields";

	public FieldView(Object object, InspectionContext inspectionContext) {
		super(NAME, object, inspectionContext);
	}

	@Override
	Component createView(ViewType viewType, Object object, InspectionContext inspectionContext) {
		return viewType == ViewType.QUICK
			? new FieldTree(object, inspectionContext)
			: new FieldTable(object, inspectionContext);
	}
}
