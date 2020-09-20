package dd.kms.marple.impl.inspector;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.marple.impl.gui.inspector.InspectionFrame;
import dd.kms.marple.impl.gui.inspector.views.ComponentHierarchyView;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.awt.*;
import java.util.List;

public class ObjectInspectorImpl implements ObjectInspector
{
	private InspectionContext	context;

	@Override
	public void setInspectionContext(InspectionContext context) {
		this.context = context;
	}

	@Override
	public void inspectComponent(ComponentHierarchy componentHierarchy) {
		ObjectInfo objectToInspect = InfoProvider.createObjectInfo(componentHierarchy.getSelectedComponent());
		ImmutableList.Builder<ObjectView> viewBuilder = ImmutableList.<ObjectView>builder()
			.add(new ComponentHierarchyView(componentHierarchy, context))
			.addAll(context.getInspectionViews(objectToInspect));
		showViews(objectToInspect, viewBuilder.build());
	}

	@Override
	public void inspectObject(ObjectInfo objectInfo) {
		ImmutableList.Builder<ObjectView> viewBuilder = ImmutableList.<ObjectView>builder()
			.addAll(context.getInspectionViews(objectInfo));
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
		InspectionFrame inspectionFrame = new InspectionFrame(context);
		inspectionFrame.setPreferredSize(new Dimension(800, 600));
		return inspectionFrame;
	}

	private void onCloseInspectionFrame() {
		context.clearHistory();
	}
}
