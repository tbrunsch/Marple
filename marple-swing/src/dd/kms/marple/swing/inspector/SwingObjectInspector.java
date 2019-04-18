package dd.kms.marple.swing.inspector;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.ObjectInspector;
import dd.kms.marple.swing.gui.InspectionFrame;
import dd.kms.marple.swing.gui.views.FieldView;
import dd.kms.marple.swing.gui.views.ComponentHierarchyView;
import dd.kms.marple.swing.gui.views.MethodView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SwingObjectInspector implements ObjectInspector<Component, Component>
{
	public static Object getHierarchyLeaf(List<Component> componentHierarchy, List<?> subcomponentHierarchy) {
		List<?> lastNonEmptyList =	!subcomponentHierarchy.isEmpty()	? subcomponentHierarchy :
									!componentHierarchy.isEmpty()		? componentHierarchy
																		: null;
		return lastNonEmptyList == null ? null : lastNonEmptyList.get(lastNonEmptyList.size()-1);
	}

	private InspectionContext<Component, Component>	inspectionContext;
	private InspectionFrame							inspectionFrame;

	@Override
	public void setInspectionContext(InspectionContext<Component, Component> inspectionContext) {
		this.inspectionContext = inspectionContext;
	}

	@Override
	public void inspectComponent(List<Component> componentHierarchy, List<?> subcomponentHierarchy) {
		Object hierarchyLeaf = getHierarchyLeaf(componentHierarchy, subcomponentHierarchy);
		ImmutableList.Builder<Component> viewBuilder = ImmutableList.<Component>builder()
			.add(new ComponentHierarchyView(componentHierarchy, subcomponentHierarchy, inspectionContext))
			.addAll(inspectionContext.getInspectionViews(hierarchyLeaf));
		showViews(hierarchyLeaf, viewBuilder.build());
	}

	@Override
	public void inspectObject(Object object) {
		ImmutableList.Builder<Component> viewBuilder = ImmutableList.<Component>builder()
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
	private void showViews(Object object, List<Component> views) {
		boolean virginInspectionFrame = inspectionFrame == null;
		if (virginInspectionFrame) {
			inspectionFrame = createInspectionFrame();
			inspectionFrame.setPreferredSize(new Dimension(800, 600));
		}
		inspectionFrame.setViews(object, views);
		if (virginInspectionFrame) {
			inspectionFrame.pack();
		}
	}

	private InspectionFrame createInspectionFrame() {
		InspectionFrame inspectionFrame = new InspectionFrame(inspectionContext);
		inspectionFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				disposeInspectionFrame();
			}
		});
		return inspectionFrame;
	}

	private void disposeInspectionFrame() {
		inspectionFrame = null;
		inspectionContext.clearHistory();
	}
}
