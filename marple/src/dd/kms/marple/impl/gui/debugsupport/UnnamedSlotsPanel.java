package dd.kms.marple.impl.gui.debugsupport;

import dd.kms.marple.api.DebugSupport;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.gui.table.ColumnDescription;
import dd.kms.marple.impl.gui.table.ColumnDescriptionBuilder;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class UnnamedSlotsPanel extends AbstractSlotPanel<Integer>
{
	UnnamedSlotsPanel(Consumer<Throwable> exceptionConsumer, InspectionContext context) {
		super("Unnamed Slots",
				"<html><p>Access unnamed slots via DebugSupport.SLOTS[0], DebugSupport.SLOTS[1] etc.</p></html>",
				exceptionConsumer,
				context);
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
		return new ActionProviderBuilder(context.getDisplayText(slotValueInfo), slotValueInfo, context)
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
