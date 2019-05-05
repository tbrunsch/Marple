package dd.kms.marple.gui.inspector.views;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.ObjectView;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.*;

public class FieldView extends JPanel implements ObjectView
{
	private static final String	NAME	= "Fields";

	private final JPanel			viewSelectionPanel			= new JPanel(new GridBagLayout());
	private final JToggleButton		quickViewToggleButton		= new JToggleButton("Quick View");
	private final JToggleButton		detailedViewToggleButton	= new JToggleButton("Detailed View");
	private final ButtonGroup		viewButtonGroup				= new ButtonGroup();

	private final JPanel			viewPanel					= new JPanel(new GridBagLayout());

	private final Object			object;
	private final InspectionContext	inspectionContext;

	public FieldView(Object object, InspectionContext inspectionContext) {
		super(new BorderLayout());

		this.object = object;
		this.inspectionContext = inspectionContext;

		setName(NAME);

		add(viewSelectionPanel,	BorderLayout.NORTH);
		add(viewPanel,			BorderLayout.CENTER);

		viewSelectionPanel.add(quickViewToggleButton, 		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, NORTHWEST, BOTH, new Insets(5, 5, 5, 5), 0, 0));
		viewSelectionPanel.add(detailedViewToggleButton, 	new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, NORTHWEST, BOTH, new Insets(5, 5, 5, 5), 0, 0));
		viewSelectionPanel.add(new JLabel(), 				new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		viewButtonGroup.add(quickViewToggleButton);
		viewButtonGroup.add(detailedViewToggleButton);

		setViewType(ViewType.QUICK);

		quickViewToggleButton.addActionListener(e -> setViewType(ViewType.QUICK));
		detailedViewToggleButton.addActionListener(e -> setViewType(ViewType.DETAILED));
	}

	@Override
	public String getViewName() {
		return NAME;
	}

	@Override
	public Component getViewComponent() {
		return this;
	}

	@Override
	public Object getViewSettings() {
		return getViewType();
	}

	@Override
	public void applyViewSettings(Object settings) {
		if (settings instanceof ViewType) {
			setViewType((ViewType) settings);
		}
	}

	private void setViewType(ViewType viewType) {
		Component view = viewType == ViewType.QUICK
							? new FieldTree(object, inspectionContext)
							: new FieldTable(object, inspectionContext);
		JToggleButton toggleButton = viewType == ViewType.QUICK ? quickViewToggleButton : detailedViewToggleButton;
		viewPanel.removeAll();
		viewPanel.add(view, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, NORTHWEST, BOTH, new Insets(5, 5, 5, 5), 0, 0));
		viewPanel.validate();
		toggleButton.setSelected(true);
	}

	private ViewType getViewType() {
		return quickViewToggleButton.isSelected() ? ViewType.QUICK : ViewType.DETAILED;
	}

	private enum ViewType { QUICK, DETAILED }
}
