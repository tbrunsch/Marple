package dd.kms.marple.swing.gui.views;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;

import static java.awt.GridBagConstraints.*;

public class FieldView extends JPanel
{
	private static final String	NAME	= "Fields";

	private final JPanel		viewSelectionPanel			= new JPanel(new GridBagLayout());
	private final JToggleButton	quickViewToggleButton		= new JToggleButton("Quick View");
	private final JToggleButton	detailedViewToggleButton	= new JToggleButton("Detailed View");
	private final ButtonGroup	viewButtonGroup				= new ButtonGroup();

	private final JPanel		viewPanel					= new JPanel(new GridBagLayout());

	private final Object					object;
	private final InspectionContext<?, ?>	inspectionContext;

	public FieldView(Object object, InspectionContext<?, ?> inspectionContext) {
		super(new BorderLayout());

		this.object = object;
		this.inspectionContext = inspectionContext;

		add(viewSelectionPanel,	BorderLayout.NORTH);
		add(viewPanel,			BorderLayout.CENTER);

		viewSelectionPanel.add(quickViewToggleButton, 		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, NORTHWEST, BOTH, new Insets(5, 5, 5, 5), 0, 0));
		viewSelectionPanel.add(detailedViewToggleButton, 	new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, NORTHWEST, BOTH, new Insets(5, 5, 5, 5), 0, 0));
		viewSelectionPanel.add(new JLabel(), 				new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		viewButtonGroup.add(quickViewToggleButton);
		viewButtonGroup.add(detailedViewToggleButton);

		setView(ViewType.QUICK);

		quickViewToggleButton.addActionListener(e -> setView(ViewType.QUICK));
		detailedViewToggleButton.addActionListener(e -> setView(ViewType.DETAILED));

		setName(NAME);
	}

	private void setView(ViewType viewType) {
		Component view = viewType == ViewType.QUICK
							? new FieldTree(object, inspectionContext)
							: new FieldTable(object, inspectionContext);
		JToggleButton toggleButton = viewType == ViewType.QUICK ? quickViewToggleButton : detailedViewToggleButton;
		viewPanel.removeAll();
		viewPanel.add(view, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, NORTHWEST, BOTH, new Insets(5, 5, 5, 5), 0, 0));
		viewPanel.validate();
		toggleButton.setSelected(true);
	}

	private enum ViewType { QUICK, DETAILED }
}
