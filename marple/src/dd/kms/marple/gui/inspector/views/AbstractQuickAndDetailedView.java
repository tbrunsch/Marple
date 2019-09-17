package dd.kms.marple.gui.inspector.views;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public abstract class AbstractQuickAndDetailedView extends JPanel implements ObjectView
{
	private final JPanel			viewSelectionPanel			= new JPanel(new GridBagLayout());
	private final JToggleButton		quickViewToggleButton		= new JToggleButton("Quick View");
	private final JToggleButton		detailedViewToggleButton	= new JToggleButton("Detailed View");
	private final ButtonGroup		viewButtonGroup				= new ButtonGroup();

	private final JPanel			viewPanel					= new JPanel(new GridBagLayout());

	private final String			name;

	private final ObjectInfo		objectInfo;
	private final InspectionContext	inspectionContext;

	public AbstractQuickAndDetailedView(String name, ObjectInfo objectInfo, InspectionContext inspectionContext) {
		super(new BorderLayout());

		this.name = name;

		this.objectInfo = objectInfo;
		this.inspectionContext = inspectionContext;

		setName(name);

		add(viewSelectionPanel,	BorderLayout.NORTH);
		add(viewPanel,			BorderLayout.CENTER);

		viewSelectionPanel.add(quickViewToggleButton, 		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, NORTHWEST, BOTH,		DEFAULT_INSETS, 0, 0));
		viewSelectionPanel.add(detailedViewToggleButton, 	new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, NORTHWEST, BOTH,		DEFAULT_INSETS, 0, 0));
		viewSelectionPanel.add(new JLabel(), 				new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL,	DEFAULT_INSETS, 0, 0));

		viewButtonGroup.add(quickViewToggleButton);
		viewButtonGroup.add(detailedViewToggleButton);

		setViewType(ViewType.QUICK);

		quickViewToggleButton.addActionListener(e -> setViewType(ViewType.QUICK));
		detailedViewToggleButton.addActionListener(e -> setViewType(ViewType.DETAILED));
	}

	protected abstract Component createView(ViewType viewType, ObjectInfo objectInfo, InspectionContext inspectionContext);

	@Override
	public String getViewName() {
		return name;
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
		Component view = createView(viewType, objectInfo, inspectionContext);
		JToggleButton toggleButton = viewType == ViewType.QUICK ? quickViewToggleButton : detailedViewToggleButton;
		toggleButton.setSelected(true);
		viewPanel.removeAll();
		viewPanel.add(view, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, NORTHWEST, BOTH, DEFAULT_INSETS, 0, 0));
		viewPanel.validate();
	}

	private ViewType getViewType() {
		return quickViewToggleButton.isSelected() ? ViewType.QUICK : ViewType.DETAILED;
	}

	protected enum ViewType { QUICK, DETAILED }
}
