package dd.kms.marple.impl.gui.inspector;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.actions.ActionWrapper;
import dd.kms.marple.impl.gui.common.CurrentObjectPanel;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.gui.inspector.views.iterableview.IterableView;
import dd.kms.marple.impl.gui.inspector.views.mapview.MapView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;

public class InspectionFrame extends JFrame implements ObjectView
{
	private final JPanel							mainPanel			= new JPanel(new BorderLayout());

	private final JPanel							navigationPanel		= new JPanel(new GridBagLayout());
	private final JButton							prevButton			= new JButton();
	private final JButton							nextButton			= new JButton();
	private final CurrentObjectPanel				currentObjectPanel;

	private final JTabbedPane 						viewPane			= new JTabbedPane();
	private final JScrollPane						viewScrollPane		= new JScrollPane(viewPane);

	private final InspectionContext					context;

	private final History<InspectionViewSettings>	history				= new History<>();

	/**
	 * {@code Runnable} that represents the last call of {@link #setViews(Object, List, Runnable)}.
	 */
	private Runnable								viewGenerator;

	/**
	 * Used to distinguish whether {@link #setViews(Object, List, Runnable)} is called by the user or because a
	 * state from the history is currently restored.
	 */
	private boolean									restoringStateFromHistory;

	private List<ObjectView>						views				= ImmutableList.of();

	public InspectionFrame(InspectionContext context) {
		this.context = context;
		this.currentObjectPanel = new CurrentObjectPanel(context);
		configure();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				history.clear();
			}
		});
	}

	@Override
	public String getViewName() {
		return "Inspect";
	}

	@Override
	public Component getViewComponent() {
		return this;
	}

	private void configure() {
		setTitle(getViewName());

		getContentPane().add(mainPanel);

		mainPanel.add(navigationPanel,	BorderLayout.NORTH);
		mainPanel.add(viewScrollPane, 	BorderLayout.CENTER);

		navigationPanel.add(prevButton,   		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,	GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));
		navigationPanel.add(currentObjectPanel,	new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,	GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));
		navigationPanel.add(nextButton,   		new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,	GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));
	}

	@Override
	public InspectionViewSettings getViewSettings() {
		Object currentObject = currentObjectPanel.getCurrentObject();
		String selectedViewName = getSelectedViewName();
		Map<String, Object> viewSettingsByViewName = new HashMap<>();
		for (ObjectView view : views) {
			Object viewSettings = view.getViewSettings();
			if (viewSettings != null) {
				viewSettingsByViewName.put(view.getViewName(), viewSettings);
			}
		}
		return new InspectionViewSettings(currentObject, viewGenerator, selectedViewName, viewSettingsByViewName);
	}

	private String getSelectedViewName() {
		int selectedViewIndex = viewPane.getSelectedIndex();
		if (selectedViewIndex < 0) {
			return null;
		}
		ObjectView selectedView = views.get(selectedViewIndex);
		return selectedView.getViewName();
	}

	private void selectView(String viewName) {
		int indexOfView = viewPane.indexOfTab(viewName);
		if (indexOfView < 0) {
			// switch from "Iterables" tab to "Maps" tab and vice versa
			indexOfView =	IterableView.NAME.equals(viewName)	? viewPane.indexOfTab(MapView.NAME) :
							MapView.NAME.equals(viewName)		? viewPane.indexOfTab(IterableView.NAME)
																: -1;
		}
		if (indexOfView >= 0) {
			viewPane.setSelectedIndex(indexOfView);
		}
	}

	@Override
	public void applyViewSettings(Object settings, ViewSettingsOrigin origin) {
		if (settings instanceof InspectionViewSettings) {
			InspectionViewSettings inspectionViewSettings = (InspectionViewSettings) settings;

			if (origin == ViewSettingsOrigin.SAME_CONTEXT) {
				currentObjectPanel.setCurrentObject(inspectionViewSettings.getCurrentObject());
			}

			String selectedViewName = inspectionViewSettings.getSelectedViewName();
			selectView(selectedViewName);

			Map<String, Object> viewSettingsByViewName = inspectionViewSettings.getViewSettingsByViewName();
			for (ObjectView view : views) {
				String viewName = view.getViewName();
				Object viewSettings = viewSettingsByViewName.get(viewName);
				if (viewSettings != null) {
					view.applyViewSettings(viewSettings, origin);
				}
			}
		}
	}

	private void applyHistoryViewSettings(InspectionViewSettings viewSettings) {
		restoringStateFromHistory = true;
		try {
			viewSettings.viewGenerator.run();
			applyViewSettings(viewSettings, ViewSettingsOrigin.SAME_CONTEXT);
		} finally {
			restoringStateFromHistory = false;
		}
	}

	public void setViews(Object object, List<ObjectView> views, Runnable viewGenerator) {
		if (!restoringStateFromHistory) {
			updateHistoryEntry();
		}

		InspectionViewSettings oldViewSettings = this.viewGenerator != null ? getViewSettings() : null;

		this.views = views;
		currentObjectPanel.setCurrentObject(object);
		this.viewGenerator = viewGenerator;

		viewPane.removeAll();
		for (ObjectView view : views) {
			viewPane.add(view.getViewComponent(), Preconditions.checkNotNull(view.getViewName(), "Missing name of view '" + view + "'"));
		}

		if (!restoringStateFromHistory) {
			if (oldViewSettings != null) {
				// apply old state to new state as good as possible
				applyViewSettings(oldViewSettings, ObjectView.ViewSettingsOrigin.OTHER_CONTEXT);
			}

			InspectionViewSettings newViewSettings = getViewSettings();
			history.add(newViewSettings);
		}

		setVisible(true);

		prevButton.setAction(new ActionWrapper(new InspectionHistoryBackAction(context, history, this::updateHistoryEntry, this::applyHistoryViewSettings)));
		nextButton.setAction(new ActionWrapper(new InspectionHistoryForwardAction(context, history, this::updateHistoryEntry, this::applyHistoryViewSettings)));
	}

	private void updateHistoryEntry() {
		if (this.viewGenerator != null) {
			InspectionViewSettings viewSettings = getViewSettings();
			history.set(viewSettings);
		}
	}

	static class InspectionViewSettings
	{
		private final Object				currentObject;
		private final Runnable				viewGenerator;
		private final String				selectedViewName;
		private final Map<String, Object>	viewSettingsByViewName;

		InspectionViewSettings(Object currentObject, Runnable viewGenerator, String selectedViewName, Map<String, Object> viewSettingsByViewName) {
			this.currentObject = currentObject;
			this.viewGenerator = viewGenerator;
			this.selectedViewName = selectedViewName;
			this.viewSettingsByViewName = viewSettingsByViewName;
		}

		Object getCurrentObject() {
			return currentObject;
		}

		Runnable getViewGenerator() {
			return viewGenerator;
		}

		String getSelectedViewName() {
			return selectedViewName;
		}

		Map<String, Object> getViewSettingsByViewName() {
			return viewSettingsByViewName;
		}
	}
}
