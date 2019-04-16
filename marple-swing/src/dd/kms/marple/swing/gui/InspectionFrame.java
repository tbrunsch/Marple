package dd.kms.marple.swing.gui;

import com.google.common.base.Preconditions;
import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InspectionFrame extends JFrame
{
	private static final Dimension INITIAL_SIZE = new Dimension(400, 300);

	private final JPanel		mainPanel			= new JPanel(new BorderLayout());

	private final JPanel		navigationPanel		= new JPanel(new GridBagLayout());
	private final JButton		prevButton			= new JButton();
	private final JButton		nextButton			= new JButton();
	private final JPanel		currentObjectPanel	= new JPanel(new GridBagLayout());
	private final JLabel		classInfoLabel		= new JLabel();
	private final JLabel		toStringLabel		= new JLabel();

	private final JTabbedPane 	viewPane			= new JTabbedPane();
	private final JScrollPane	viewScrollPane		= new JScrollPane(viewPane);

	private final InspectionContext<Component, Component>	inspectionContext;

	private boolean											initializing;
	private String											lastSelectedTabTitle;

	public InspectionFrame(InspectionContext<Component, Component> inspectionContext) {
		this.inspectionContext = inspectionContext;
		configure();
	}

	private void configure() {
		setTitle("Object Inspection Manager");

		getContentPane().add(mainPanel);

		mainPanel.add(navigationPanel,	BorderLayout.NORTH);
		mainPanel.add(viewScrollPane, 	BorderLayout.CENTER);

		navigationPanel.add(prevButton,   		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		navigationPanel.add(currentObjectPanel,	new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		navigationPanel.add(nextButton,   		new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		currentObjectPanel.setBorder(BorderFactory.createEtchedBorder());
		currentObjectPanel.add(toStringLabel,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		currentObjectPanel.add(classInfoLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		setSize(INITIAL_SIZE);

		viewPane.addChangeListener(e -> onTabChanged());
	}

	public void setViews(Object object, List<? extends Component> views) {
		initializing = true;
		toStringLabel.setText('"' + inspectionContext.getDisplayText(object) + '"');
		classInfoLabel.setText(object.getClass().toString());
		viewPane.removeAll();

		for (Component view : views) {
			viewPane.add(view, Preconditions.checkNotNull(view.getName(), "Missing name of view '" + view + "'"));
		}

		if (lastSelectedTabTitle != null) {
			int indexOfLastSelectedTab = viewPane.indexOfTab(lastSelectedTabTitle);
			if (indexOfLastSelectedTab >= 0) {
				viewPane.setSelectedIndex(indexOfLastSelectedTab);
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
		int selectedIndex = viewPane.getSelectedIndex();
		if (selectedIndex >= 0) {
			lastSelectedTabTitle = viewPane.getTitleAt(selectedIndex);
		}
	}
}
