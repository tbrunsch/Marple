package dd.kms.marple.actions.debugsupport;

import com.google.common.collect.Lists;
import dd.kms.marple.DebugSupport;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.gui.common.WindowManager;
import dd.kms.marple.gui.evaluator.VariablePanel;
import dd.kms.marple.gui.table.ColumnDescription;
import dd.kms.marple.gui.table.ColumnDescriptionBuilder;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class NamedSlotsPanel extends AbstractSlotPanel<String>
{
	private final JButton	addSlotButton		= new JButton("Add slot");
	private final JButton	deleteSlotsButton	= new JButton("Delete selected slots");
	private final JButton	variablesButton		= new JButton("Open variable dialog");

	NamedSlotsPanel(Consumer<Throwable> exceptionConsumer, InspectionContext inspectionContext) {
		super("Named Slots",
				"<html><p>Access named slots via DebugSupport.getSlotValue and DebugSupport.setSlotValue<br/>Import named slots as variables or export variables as named slots</p></html>",
				exceptionConsumer,
				inspectionContext);

		int xPos = 0;
		add(addSlotButton,		new GridBagConstraints(xPos++, yPos, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(deleteSlotsButton,	new GridBagConstraints(xPos++, yPos, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(variablesButton,	new GridBagConstraints(xPos++, yPos, 1, 1, 1.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));

		updateEnabilities();

		addListeners();
	}

	private void addListeners() {
		addSlotButton.addActionListener(e -> addSlot());
		deleteSlotsButton.addActionListener(e -> deleteSelectedSlots());
		variablesButton.addActionListener(e -> openVariableDialog());

		table.getSelectionModel().addListSelectionListener(e -> updateEnabilities());
	}

	@Override
	List<String> createTableList() {
		return Lists.newArrayList(DebugSupport.getSlotNames());
	}

	@Override
	List<ColumnDescription<String>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<String>("Slot Name", String.class, name -> name).editorSettings(this::setSlotName).build(),
			new ColumnDescriptionBuilder<>("Value", ActionProvider.class, this::getSlotValueAsActionProvider).editorSettings(this::setSlotValue).build()
		);
	}

	@Override
	void onContentChanged() {
		updateEnabilities();
	}

	private ActionProvider getSlotValueAsActionProvider(String slotName) {
		Object slotValue = DebugSupport.getSlotValue(slotName);
		ObjectInfo slotValueInfo = InfoProvider.createObjectInfo(slotValue);
		return new ActionProviderBuilder(inspectionContext.getDisplayText(slotValueInfo), slotValueInfo, inspectionContext)
			.evaluateAs("DebugSupport.getSlotValue(\"" + slotName + "\")")
			.suggestVariableName(slotName)
			.executeDefaultAction(false)
			.build();
	}

	private void setSlotName(List<String> slotNames, int slotIndex, Object slotName) {
		if (slotName instanceof String || slotName == null) {
			String oldSlotName = slotNames.get(slotIndex);
			String newSlotName = (String) slotName;
			if (DebugSupport.renameSlot(oldSlotName, newSlotName)) {
				slotNames.set(slotIndex, newSlotName);
			}
		}
	}

	private void setSlotValue(List<String> slotNames, int slotIndex, Object valueInfoAsObject) {
		ObjectInfo valueInfo = (ObjectInfo) valueInfoAsObject;
		Object value = valueInfo.getObject();
		String slotName = slotNames.get(slotIndex);
		DebugSupport.setSlotValue(slotName, value);
	}

	private void addSlot() {
		Collection<String> slotNames = DebugSupport.getSlotNames();
		String suggestedSlotName = "slot";
		int i = 1;
		String slotName = suggestedSlotName;
		while (slotNames.contains(slotName)) {
			slotName = suggestedSlotName + ++i;
		}
		DebugSupport.setSlotValue(slotName, null);
		tableList.add(slotName);

		fireTableChanged();

		int newRow = tableList.size() - 1;
		table.requestFocus();
		table.editCellAt(newRow, 0);
	}

	private void deleteSelectedSlots() {
		int[] selectedRows = table.getSelectedRows();

		// remove in descending order
		int[] rows = IntStream.of(selectedRows).map(i -> -i).sorted().map(i -> -i).toArray();
		for (int row : rows) {
			String slotName = tableList.get(row);
			DebugSupport.deleteSlot(slotName);
			tableList.remove(row);
		}

		fireTableChanged();
	}

	private void openVariableDialog() {
		WindowManager.showInFrame(VariablePanel.WINDOW_TITLE, this::createVariablePanel, this::onShowVariableDialog, VariablePanel::updateContent);
	}

	private VariablePanel createVariablePanel() {
		return new VariablePanel(inspectionContext);
	}

	private void onShowVariableDialog(VariablePanel variablePanel) {
		// currently we do nothing at all
	}

	private void updateEnabilities() {
		boolean rowsSelected = table.getSelectedRows().length > 0;
		deleteSlotsButton.setEnabled(rowsSelected);
	}
}
