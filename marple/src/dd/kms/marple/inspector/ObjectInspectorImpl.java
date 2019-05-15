package dd.kms.marple.inspector;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.gui.ObjectView;
import dd.kms.marple.gui.common.WindowManager;
import dd.kms.marple.gui.inspector.InspectionFrame;
import dd.kms.marple.gui.inspector.views.ComponentHierarchyView;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

class ObjectInspectorImpl implements ObjectInspector
{
	private InspectionContext	inspectionContext;

	@Override
	public void setInspectionContext(InspectionContext inspectionContext) {
		this.inspectionContext = inspectionContext;
	}

	@Override
	public void inspectComponent(List<Component> componentHierarchy, List<?> subcomponentHierarchy) {
		Object hierarchyLeaf = ComponentHierarchyModels.getHierarchyLeaf(componentHierarchy, subcomponentHierarchy);
		ImmutableList.Builder<ObjectView> viewBuilder = ImmutableList.<ObjectView>builder()
			.add(new ComponentHierarchyView(componentHierarchy, subcomponentHierarchy, inspectionContext))
			.addAll(inspectionContext.getInspectionViews(hierarchyLeaf));
		showViews(hierarchyLeaf, viewBuilder.build());
	}

	@Override
	public void inspectObject(Object object) {
		ImmutableList.Builder<ObjectView> viewBuilder = ImmutableList.<ObjectView>builder()
			.addAll(inspectionContext.getInspectionViews(object));
		showViews(object, viewBuilder.build());
	}

	@Override
	public void highlightComponent(Component component) {
		Runnable componentHighlighter = new ComponentHighlighter(component);
		new Thread(componentHighlighter).start();
	}

	/*
	 * Inspection Frame Handling
	 */
	private void showViews(Object object, List<ObjectView> views) {
		InspectionFrame inspectionFrame = WindowManager.getWindow(ObjectInspector.class, this::createInspectionFrame, this::onCloseInspectionFrame);
		inspectionFrame.setViews(object, views);
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
