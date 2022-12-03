package dd.kms.marple.impl.gui.inspector;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.gui.inspector.InspectionFrame.InspectionViewSettings;

import java.util.function.Consumer;
import java.util.function.Function;

class InspectionHistoryBackAction implements InspectionAction
{
	private final History<InspectionViewSettings>			history;
	private final Function<InspectionViewSettings, String>	stringRepresentationProvider;
	private final Runnable									updateHistoryEntryRunnable;
	private final Consumer<InspectionViewSettings>			prevSettingsConsumer;

	InspectionHistoryBackAction(History<InspectionViewSettings> history, Function<InspectionViewSettings, String> stringRepresentationProvider, Runnable updateHistoryEntryRunnable, Consumer<InspectionViewSettings> prevSettingsConsumer) {
		this.history = history;
		this.stringRepresentationProvider = stringRepresentationProvider;
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
		return stringRepresentationProvider.apply(prevSettings);
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
