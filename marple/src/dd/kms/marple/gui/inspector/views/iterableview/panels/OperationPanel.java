package dd.kms.marple.gui.inspector.views.iterableview.panels;

import com.google.common.collect.ImmutableMap;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.inspector.views.iterableview.settings.Operation;
import dd.kms.marple.gui.inspector.views.iterableview.settings.OperationSettings;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_DISTANCE;
import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class OperationPanel extends JPanel
{
	private final JLabel		operationTypeLabel		= new JLabel("Operation:");
	private final JToggleButton	filterTB				= new JToggleButton();
	private final JToggleButton	mapTB					= new JToggleButton();
	private final JToggleButton	forEachTB				= new JToggleButton();
	private final JToggleButton	collectTB				= new JToggleButton();
	private final JToggleButton	toMapTB					= new JToggleButton();

	private final ButtonGroup	operationButtonGroup	= new ButtonGroup();

	private final FilterSettingsPanel	filterSettingsPanel;
	private final MapSettingsPanel		mapSettingsPanel;
	private final ForEachSettingsPanel	forEachSettingsPanel;
	private final CollectSettingsPanel	collectSettingsPanel;
	private final ToMapSettingsPanel	toMapSettingsPanel;

	private final Map<Operation, JToggleButton> operationToButton = ImmutableMap.<Operation, JToggleButton>builder()
																	.put(Operation.FILTER,		filterTB)
																	.put(Operation.MAP,			mapTB)
																	.put(Operation.FOR_EACH,	forEachTB)
																	.put(Operation.COLLECT,		collectTB)
																	.put(Operation.TO_MAP,		toMapTB)
																	.build();

	private final Map<Operation, AbstractOperationSettingsPanel> operationToSettingsPanel;

	private final JLabel						settingsLabel			= new JLabel("Settings:");
	private final JPanel						settingsPanel			= new JPanel(new GridBagLayout());

	private final JButton						runButton				= new JButton("Run");

	public OperationPanel(TypeInfo commonElementType, InspectionContext context) {
		super(new GridBagLayout());

		filterSettingsPanel		= new FilterSettingsPanel(commonElementType, context);
		mapSettingsPanel		= new MapSettingsPanel(commonElementType, context);
		forEachSettingsPanel	= new ForEachSettingsPanel(commonElementType, context);
		collectSettingsPanel	= new CollectSettingsPanel(commonElementType, context);
		toMapSettingsPanel		= new ToMapSettingsPanel(commonElementType, context);

		operationToSettingsPanel = ImmutableMap.<Operation, AbstractOperationSettingsPanel>builder()
			.put(Operation.FILTER,		filterSettingsPanel)
			.put(Operation.MAP,			mapSettingsPanel)
			.put(Operation.FOR_EACH,	forEachSettingsPanel)
			.put(Operation.COLLECT,		collectSettingsPanel)
			.put(Operation.TO_MAP,		toMapSettingsPanel)
			.build();

		setBorder(BorderFactory.createTitledBorder("Operation"));

		initButtons(operationToButton, operationButtonGroup, Operation.FILTER);

		int yPos = 0;

		int xPos = 0;
		add(operationTypeLabel, new GridBagConstraints(xPos++, yPos, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		for (JToggleButton button : operationToButton.values()) {
			add(button, 		new GridBagConstraints(xPos++, yPos, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		}
		yPos++;

		xPos = 0;
		add(settingsLabel,		new GridBagConstraints(xPos++, yPos, 1, 1, 0.0, 1.0, NORTHWEST, NONE, new Insets(2*DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE), 0, 0));
		int width = operationToButton.size();
		add(settingsPanel,		new GridBagConstraints(xPos, yPos, width, 1, 1.0, 1.0, CENTER, BOTH, new Insets(0, 0, 0, 0), 0, 0));
		xPos += width;
		add(runButton,			new GridBagConstraints(xPos++, yPos++, 1, 1, 0.0, 0.0, SOUTHEAST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		for (JToggleButton button : operationToButton.values()) {
			button.addActionListener(e -> showOperationSettings());
		}
		showOperationSettings();
	}

	public void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
		for (AbstractOperationSettingsPanel operationSettingsPanel : operationToSettingsPanel.values()) {
			operationSettingsPanel.setExceptionConsumer(exceptionConsumer);
		}
	}

	public void setAction(Runnable action) {
		for (AbstractOperationSettingsPanel operationSettingsPanel : operationToSettingsPanel.values()) {
			operationSettingsPanel.setAction(action);
		}
		runButton.addActionListener(e -> action.run());
	}

	private <T> void initButtons(Map<T, JToggleButton> valueToButtonMap, ButtonGroup buttonGroup, T initialValue) {
		for (T value : valueToButtonMap.keySet()) {
			JToggleButton button = valueToButtonMap.get(value);
			button.setText(value.toString());
			buttonGroup.add(button);
		}
		setSelectedValue(valueToButtonMap, initialValue);
	}

	private <T> T getSelectedValue(Map<T, JToggleButton> valueToButtonMap) {
		for (T value : valueToButtonMap.keySet()) {
			JToggleButton button = valueToButtonMap.get(value);
			if (button.isSelected()) {
				return value;
			}
		}
		return null;
	}

	private <T> void setSelectedValue(Map<T, JToggleButton> valueToButtonMap, T value) {
		valueToButtonMap.get(value).setSelected(true);
	}

	public OperationSettings getSettings() {
		Operation operation = getSelectedValue(operationToButton);
		AbstractOperationSettingsPanel operationSettingsPanel = operationToSettingsPanel.get(operation);
		return operationSettingsPanel.getSettings();
	}

	public void setSettings(OperationSettings settings) {
		Operation operation = settings.getOperation();
		setSelectedValue(operationToButton, operation);
		AbstractOperationSettingsPanel operationSettingsPanel = operationToSettingsPanel.get(operation);
		operationSettingsPanel.setSettings(settings);
		showOperationSettings();
	}

	private void showOperationSettings() {
		Operation operation = getSelectedValue(operationToButton);
		AbstractOperationSettingsPanel operationSettingsPanel = operationToSettingsPanel.get(operation);
		settingsPanel.removeAll();
		settingsPanel.add(operationSettingsPanel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, new Insets(0, 0, 0, 0), 0, 0));
		settingsPanel.revalidate();
		settingsPanel.repaint();
	}
}
