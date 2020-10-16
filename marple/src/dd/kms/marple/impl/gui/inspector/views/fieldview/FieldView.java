package dd.kms.marple.impl.gui.inspector.views.fieldview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.gui.inspector.views.AbstractQuickAndDetailedView;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public class FieldView extends AbstractQuickAndDetailedView
{
	private static final String	NAME	= "Fields";

	public FieldView(ObjectInfo objectInfo, InspectionContext context) {
		super(NAME, objectInfo, context);
	}

	@Override
	protected ObjectView createView(ViewType viewType, ObjectInfo objectInfo, InspectionContext context) {
		return viewType == ViewType.QUICK
			? new FieldTree(objectInfo, context)
			: new FieldTable(objectInfo, context);
	}
}
