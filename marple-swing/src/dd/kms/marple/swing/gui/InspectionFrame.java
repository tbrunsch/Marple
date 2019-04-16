package dd.kms.marple.swing.gui;

import com.google.common.base.Preconditions;
import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InspectionFrame extends JFrame
{
	private static final Dimension INITIAL_SIZE = new Dimension(400, 300);

	private final JButton		prevButton			= new JButton();
	private final JButton		nextButton			= new JButton();

	private final JPanel		contentPanel		= new JPanel(new BorderLayout());

	private final JPanel		objectOverviewPanel	= new JPanel(new GridBagLayout());
	private final JLabel		classInfoLabel		= new JLabel();
	private final JLabel		toStringLabel		= new JLabel();

	private final JTabbedPane  	tabbedPane			= new JTabbedPane();

	private final InspectionContext<Component, Component>	inspectionContext;

	private boolean											initializing;
	private String											lastSelectedTabTitle;

	public InspectionFrame(InspectionContext<Component, Component> inspectionContext) {
		this.inspectionContext = inspectionContext;
		configure();
	}

	private void configure() {
		setTitle("Object Inspection Manager");

		JPanel mainPanel = new JPanel(new GridBagLayout());
		getContentPane().add(mainPanel);

		int xPos = 0;
		mainPanel.add(prevButton,   new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		mainPanel.add(contentPanel, new GridBagConstraints(xPos++, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		mainPanel.add(nextButton,   new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		contentPanel.add(objectOverviewPanel, BorderLayout.NORTH);
		contentPanel.add(new JScrollPane(tabbedPane), BorderLayout.CENTER);

		objectOverviewPanel.setBorder(BorderFactory.createEtchedBorder());
		objectOverviewPanel.add(toStringLabel,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		objectOverviewPanel.add(classInfoLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		setSize(INITIAL_SIZE);

		tabbedPane.addChangeListener(e -> onTabChanged());
	}

	public void setViews(Object object, List<? extends Component> views) {
		initializing = true;
		toStringLabel.setText('"' + inspectionContext.getDisplayText(object) + '"');
		classInfoLabel.setText(object.getClass().toString());
		tabbedPane.removeAll();

		for (Component view : views) {
			tabbedPane.add(view, Preconditions.checkNotNull(view.getName(), "Missing name of view '" + view + "'"));
		}

		if (lastSelectedTabTitle != null) {
			int indexOfLastSelectedTab = tabbedPane.indexOfTab(lastSelectedTabTitle);
			if (indexOfLastSelectedTab >= 0) {
				tabbedPane.setSelectedIndex(indexOfLastSelectedTab);
			}
		}
		setVisible(true);
		initializing = false;

		prevButton.setAction(new ActionWrapper(inspectionContext.createHistoryBackAction()));
		nextButton.setAction(new ActionWrapper(inspectionContext.createHistoryForwardAction()));
	}

	/*
	 * Event Handling
	 */
	private void onTabChanged() {
		if (initializing) {
			return;
		}
		int selectedIndex = tabbedPane.getSelectedIndex();
		if (selectedIndex >= 0) {
			lastSelectedTabTitle = tabbedPane.getTitleAt(selectedIndex);
		}
	}
}
