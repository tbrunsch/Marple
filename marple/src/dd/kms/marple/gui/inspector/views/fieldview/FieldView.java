package dd.kms.marple.gui.inspector.views.fieldview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.inspector.views.AbstractQuickAndDetailedView;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.awt.*;

public class FieldView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Fields";

	public FieldView(ObjectInfo objectInfo, InspectionContext inspectionContext) {
		super(NAME, objectInfo, inspectionContext);
	}

	@Override
	protected Component createView(ViewType viewType, ObjectInfo objectInfo, InspectionContext inspectionContext) {
		return viewType == ViewType.QUICK
			? new FieldTree(objectInfo, true, inspectionContext)
			: new FieldTable(objectInfo, inspectionContext);
	}
}
