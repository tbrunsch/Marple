package dd.kms.marple.actions.debugsupport;

import dd.kms.marple.DebugSupport;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.gui.table.ColumnDescription;
import dd.kms.marple.gui.table.ColumnDescriptionBuilder;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class UnnamedSlotsPanel extends AbstractSlotPanel<Integer>
{
	private static final SlotData[]	SLOT_DATA	= {
		new SlotData(() -> DebugSupport.SLOT_0, o -> DebugSupport.SLOT_0 = o),
		new SlotData(() -> DebugSupport.SLOT_1, o -> DebugSupport.SLOT_1 = o),
		new SlotData(() -> DebugSupport.SLOT_2, o -> DebugSupport.SLOT_2 = o),
		new SlotData(() -> DebugSupport.SLOT_3, o -> DebugSupport.SLOT_3 = o),
		new SlotData(() -> DebugSupport.SLOT_4, o -> DebugSupport.SLOT_4 = o)
	};

	UnnamedSlotsPanel(Consumer<Throwable> exceptionConsumer, InspectionContext inspectionContext) {
		super("Unnamed Slots",
				"<html><p>Access unnamed slots via DebugSupport.SLOT_0, DebugSupport.SLOT_1 etc.</p></html>",
				exceptionConsumer,
				inspectionContext);
	}

	@Override
	List<Integer> createTableList() {
		return IntStream.range(0, SLOT_DATA.length).boxed().collect(Collectors.toList());
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
		return "SLOT_" + slotIndex;
	}

	private ActionProvider getSlotValueAsActionProvider(Integer slotIndex) {
		Object slotValue = SLOT_DATA[slotIndex].getGetter().get();
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
		SLOT_DATA[slotIndex].getSetter().accept(value);
	}

	private static class SlotData
	{
		private final Supplier<Object>	getter;
		private final Consumer<Object>	setter;

		SlotData(Supplier<Object> getter, Consumer<Object> setter) {
			this.getter = getter;
			this.setter = setter;
		}

		Supplier<Object> getGetter() {
			return getter;
		}

		Consumer<Object> getSetter() {
			return setter;
		}
	}
}
