package dd.kms.marple.impl.gui.inspector.views;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public abstract class AbstractQuickAndDetailedView extends JPanel implements ObjectView
{
	private final JPanel				viewSelectionPanel			= new JPanel(new GridBagLayout());
	private final JToggleButton			quickViewToggleButton		= new JToggleButton("Quick View");
	private final JToggleButton			detailedViewToggleButton	= new JToggleButton("Detailed View");
	private final ButtonGroup			viewButtonGroup				= new ButtonGroup();

	private final JPanel				viewPanel					= new JPanel(new GridBagLayout());
	private ViewType					currentViewType;
	private ObjectView					currentView;

	private final String				name;

	private final ObjectInfo			objectInfo;
	private final InspectionContext		context;

	private final Map<ViewType, Object>	settingsByViewType = new HashMap<>();

	public AbstractQuickAndDetailedView(String name, ObjectInfo objectInfo, InspectionContext context) {
		super(new BorderLayout());

		this.name = name;

		this.objectInfo = objectInfo;
		this.context = context;

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

	protected abstract ObjectView createView(ViewType viewType, ObjectInfo objectInfo, InspectionContext context);

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
		storeCurrentViewSettings();
		ViewType viewType = getViewType();
		Object quickViewSettings = settingsByViewType.get(ViewType.QUICK);
		Object detailedViewSettings = settingsByViewType.get(ViewType.DETAILED);
		return new QuickAndDetailedViewSettings(viewType, quickViewSettings, detailedViewSettings);
	}

	@Override
	public void applyViewSettings(Object settings, ViewSettingsOrigin origin) {
		if (settings instanceof QuickAndDetailedViewSettings) {
			QuickAndDetailedViewSettings viewSettings = (QuickAndDetailedViewSettings) settings;
			setViewType(viewSettings.getViewType());
			if (origin == ViewSettingsOrigin.SAME_CONTEXT) {
				settingsByViewType.put(ViewType.QUICK, viewSettings.getQuickViewSettings());
				settingsByViewType.put(ViewType.DETAILED, viewSettings.getDetailedViewSettings());
				loadCurrentViewSettings();
			}
		}
	}

	private void setViewType(ViewType viewType) {
		storeCurrentViewSettings();
		currentViewType = viewType;
		currentView = createView(viewType, objectInfo, context);
		loadCurrentViewSettings();

		JToggleButton toggleButton = viewType == ViewType.QUICK ? quickViewToggleButton : detailedViewToggleButton;
		toggleButton.setSelected(true);
		viewPanel.removeAll();
		viewPanel.add(currentView.getViewComponent(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, NORTHWEST, BOTH, DEFAULT_INSETS, 0, 0));
		viewPanel.validate();
	}

	private void storeCurrentViewSettings() {
		if (currentView != null) {
			Object currentViewSettings = currentView.getViewSettings();
			settingsByViewType.put(currentViewType, currentViewSettings);
		}
	}

	private void loadCurrentViewSettings() {
		if (currentView != null) {
			Object settings = settingsByViewType.get(currentViewType);
			if (settings != null) {
				currentView.applyViewSettings(settings, ViewSettingsOrigin.SAME_CONTEXT);
			}
		}
	}

	private ViewType getViewType() {
		return quickViewToggleButton.isSelected() ? ViewType.QUICK : ViewType.DETAILED;
	}

	protected enum ViewType { QUICK, DETAILED }

	private static class QuickAndDetailedViewSettings
	{
		private final ViewType	viewType;
		private final Object	quickViewSettings;
		private final Object	detailedViewSettings;

		QuickAndDetailedViewSettings(ViewType viewType, Object quickViewSettings, Object detailedViewSettings) {
			this.viewType = viewType;
			this.quickViewSettings = quickViewSettings;
			this.detailedViewSettings = detailedViewSettings;
		}

		ViewType getViewType() {
			return viewType;
		}

		Object getQuickViewSettings() {
			return quickViewSettings;
		}

		Object getDetailedViewSettings() {
			return detailedViewSettings;
		}
	}
}
