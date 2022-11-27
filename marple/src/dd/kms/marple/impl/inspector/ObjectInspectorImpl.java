package dd.kms.marple.impl.inspector;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.marple.impl.gui.inspector.InspectionFrame;
import dd.kms.marple.impl.gui.inspector.views.ComponentHierarchyView;

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
		Object object = componentHierarchy.getSelectedComponent();
		ImmutableList.Builder<ObjectView> viewBuilder = ImmutableList.<ObjectView>builder()
			.add(new ComponentHierarchyView(componentHierarchy, context))
			.addAll(context.getInspectionViews(object));
		showViews(object, viewBuilder.build());
	}

	@Override
	public void inspectObject(Object object) {
		ImmutableList.Builder<ObjectView> viewBuilder = ImmutableList.<ObjectView>builder()
			.addAll(context.getInspectionViews(object));
		showViews(object, viewBuilder.build());
	}

	/*
	 * Inspection Frame Handling
	 */
	public InspectionFrame getInspectionFrame() {
		return WindowManager.getWindow(ObjectInspector.class, this::createInspectionFrame);
	}

	private void showViews(Object object, List<ObjectView> views) {
		InspectionFrame inspectionFrame = getInspectionFrame();
		inspectionFrame.setViews(object, views);
	}

	private InspectionFrame createInspectionFrame() {
		InspectionFrame inspectionFrame = new InspectionFrame(context);
		inspectionFrame.setPreferredSize(new Dimension(800, 600));
		return inspectionFrame;
	}
}
