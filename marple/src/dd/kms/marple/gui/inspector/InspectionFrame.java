package dd.kms.marple.gui.inspector;

import com.google.common.base.Preconditions;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionWrapper;
import dd.kms.marple.gui.common.CurrentObjectPanel;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;

public class InspectionFrame extends JFrame
{
	private final JPanel				mainPanel			= new JPanel(new BorderLayout());

	private final JPanel				navigationPanel		= new JPanel(new GridBagLayout());
	private final JButton				prevButton			= new JButton();
	private final JButton				nextButton			= new JButton();
	private final CurrentObjectPanel	currentObjectPanel;

	private final JTabbedPane 			viewPane			= new JTabbedPane();
	private final JScrollPane			viewScrollPane		= new JScrollPane(viewPane);

	private final InspectionContext		inspectionContext;

	private String						lastSelectedViewName;
	private Object						lastSelectedViewSettings;
	private List<ObjectView>			views;

	public InspectionFrame(InspectionContext context) {
		this.inspectionContext = context;
		this.currentObjectPanel = new CurrentObjectPanel(context);
		configure();
	}

	private void configure() {
		setTitle("Object Inspection Manager");

		getContentPane().add(mainPanel);

		mainPanel.add(navigationPanel,	BorderLayout.NORTH);
		mainPanel.add(viewScrollPane, 	BorderLayout.CENTER);

		navigationPanel.add(prevButton,   		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,	GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));
		navigationPanel.add(currentObjectPanel,	new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,	GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));
		navigationPanel.add(nextButton,   		new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,	GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));
	}

	public void setViews(ObjectInfo objectInfo, List<ObjectView> views) {
		storeLastViewSettings();
		this.views = views;
		currentObjectPanel.setCurrentObject(objectInfo);
		viewPane.removeAll();

		for (ObjectView view : views) {
			viewPane.add(view.getViewComponent(), Preconditions.checkNotNull(view.getViewName(), "Missing name of view '" + view + "'"));
		}

		if (lastSelectedViewName != null) {
			int indexOfLastSelectedView = viewPane.indexOfTab(lastSelectedViewName);
			if (indexOfLastSelectedView >= 0) {
				viewPane.setSelectedIndex(indexOfLastSelectedView);
				views.get(indexOfLastSelectedView).applyViewSettings(lastSelectedViewSettings);
			}
		}
		setVisible(true);

		prevButton.setAction(new ActionWrapper(inspectionContext.createHistoryBackAction()));
		nextButton.setAction(new ActionWrapper(inspectionContext.createHistoryForwardAction()));
	}

	private void storeLastViewSettings() {
		int selectedViewIndex = viewPane.getSelectedIndex();
		if (selectedViewIndex >= 0) {
			ObjectView selectedView = views.get(selectedViewIndex);
			lastSelectedViewName = selectedView.getViewName();
			lastSelectedViewSettings = selectedView.getViewSettings();
		}
	}
}
