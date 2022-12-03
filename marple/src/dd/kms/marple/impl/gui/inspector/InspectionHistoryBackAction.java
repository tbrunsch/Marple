package dd.kms.marple.impl.gui.inspector;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.gui.inspector.InspectionFrame.InspectionViewSettings;

import java.util.function.Consumer;

class InspectionHistoryBackAction implements InspectionAction
{
	private final InspectionContext					context;
	private final History<InspectionViewSettings>	history;
	private final Runnable							updateHistoryEntryRunnable;
	private final Consumer<InspectionViewSettings>	prevSettingsConsumer;

	InspectionHistoryBackAction(InspectionContext context, History<InspectionViewSettings> history, Runnable updateHistoryEntryRunnable, Consumer<InspectionViewSettings> prevSettingsConsumer) {
		this.context = context;
		this.history = history;
		this.updateHistoryEntryRunnable = updateHistoryEntryRunnable;
		this.prevSettingsConsumer = prevSettingsConsumer;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return "Back";
	}

	@Override
	public String getDescription() {
		if (!isEnabled()) {
			return null;
		}
		InspectionViewSettings prevSettings = history.peekPreviousElement();
		Object currentObject = prevSettings.getCurrentObject();
		return context.getDisplayText(currentObject);
	}

	@Override
	public boolean isEnabled() {
		return history.canGoBack();
	}

	@Override
	public void perform() {
		updateHistoryEntryRunnable.run();
		InspectionViewSettings prevSettings = history.peekPreviousElement();
		history.goBack();
		prevSettingsConsumer.accept(prevSettings);
	}
}
