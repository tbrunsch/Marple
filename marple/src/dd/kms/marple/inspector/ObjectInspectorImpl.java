package dd.kms.marple.inspector;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.ComponentHierarchy;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.common.WindowManager;
import dd.kms.marple.gui.inspector.InspectionFrame;
import dd.kms.marple.gui.inspector.views.ComponentHierarchyView;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.awt.*;
import java.util.List;

class ObjectInspectorImpl implements ObjectInspector
{
	private InspectionContext	inspectionContext;

	@Override
	public void setInspectionContext(InspectionContext inspectionContext) {
		this.inspectionContext = inspectionContext;
	}

	@Override
	public void inspectComponent(ComponentHierarchy componentHierarchy) {
		ObjectInfo objectToInspect = InfoProvider.createObjectInfo(componentHierarchy.getSelectedComponent());
		ImmutableList.Builder<ObjectView> viewBuilder = ImmutableList.<ObjectView>builder()
			.add(new ComponentHierarchyView(componentHierarchy, inspectionContext))
			.addAll(inspectionContext.getInspectionViews(objectToInspect));
		showViews(objectToInspect, viewBuilder.build());
	}

	@Override
	public void inspectObject(ObjectInfo objectInfo) {
		ImmutableList.Builder<ObjectView> viewBuilder = ImmutableList.<ObjectView>builder()
			.addAll(inspectionContext.getInspectionViews(objectInfo));
		showViews(objectInfo, viewBuilder.build());
	}

	/*
	 * Inspection Frame Handling
	 */
	private void showViews(ObjectInfo objectInfo, List<ObjectView> views) {
		InspectionFrame inspectionFrame = WindowManager.getWindow(ObjectInspector.class, this::createInspectionFrame, this::onCloseInspectionFrame);
		inspectionFrame.setViews(objectInfo, views);
	}

	private InspectionFrame createInspectionFrame() {
		InspectionFrame inspectionFrame = new InspectionFrame(inspectionContext);
		inspectionFrame.setPreferredSize(new Dimension(800, 600));
		return inspectionFrame;
	}

	private void onCloseInspectionFrame() {
		inspectionContext.clearHistory();
	}
}
