package dd.kms.marple.gui.search;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

public class InstanceSearchFrame extends JFrame
{
	private final JPanel				mainPanel			= new JPanel(new GridBagLayout());

	private final InstanceSearchPanel	searchPanel;

	public InstanceSearchFrame(InspectionContext inspectionContext) {
		this.searchPanel = new InstanceSearchPanel(inspectionContext);
		searchPanel.setInspectionContext(inspectionContext);
		configure();
	}

	private void configure() {
		setTitle("Find Instances");

		getContentPane().add(mainPanel);

		mainPanel.add(searchPanel, 	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	public void setRoot(Object root) {
		searchPanel.setRoot(root);
	}

	public void setTarget(Object target) {
		searchPanel.setTarget(target);
	}
}
