package dd.kms.marple.actions.debugsupport;

import dd.kms.marple.DebugSupport;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.gui.table.ColumnDescription;
import dd.kms.marple.gui.table.ColumnDescriptionBuilder;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class UnnamedSlotsPanel extends AbstractSlotPanel<Integer>
{
	UnnamedSlotsPanel(Consumer<Throwable> exceptionConsumer, InspectionContext inspectionContext) {
		super("Unnamed Slots",
				"<html><p>Access unnamed slots via DebugSupport.SLOTS[0], DebugSupport.SLOTS[1] etc.</p></html>",
				exceptionConsumer,
				inspectionContext);
	}

	@Override
	List<Integer> createTableList() {
		return IntStream.range(0, DebugSupport.SLOTS.length).boxed().collect(Collectors.toList());
	}

	@Override
	List<ColumnDescription<Integer>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<>("Slot", String.class, this::getUnnamedSlotName).build(),
			new ColumnDescriptionBuilder<>("Value", ActionProvider.class, this::getSlotValueAsActionProvider).editorSettings(this::setSlotValue).build()
		);
	}

	@Override
	void onContentChanged() {
		// currently there is nothing to do
	}

	private String getUnnamedSlotName(int slotIndex) {
		return "SLOTS[" + slotIndex + "]";
	}

	private ActionProvider getSlotValueAsActionProvider(Integer slotIndex) {
		Object slotValue = DebugSupport.SLOTS[slotIndex];
		ObjectInfo slotValueInfo = InfoProvider.createObjectInfo(slotValue);
		String unnamedSlotName = getUnnamedSlotName(slotIndex);
		return new ActionProviderBuilder(inspectionContext.getDisplayText(slotValueInfo), slotValueInfo, inspectionContext)
			.evaluateAs("DebugSupport." + unnamedSlotName)
			.suggestVariableName(unnamedSlotName)
			.executeDefaultAction(false)
			.build();
	}

	private void setSlotValue(List<Integer> slotIndices, int slotIndex, Object valueInfoAsObject) {
		ObjectInfo valueInfo = (ObjectInfo) valueInfoAsObject;
		Object value = valueInfo.getObject();
		DebugSupport.SLOTS[slotIndex] = value;
	}
}
