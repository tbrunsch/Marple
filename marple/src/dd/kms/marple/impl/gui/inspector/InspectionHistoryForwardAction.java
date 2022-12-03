package dd.kms.marple.impl.gui.inspector;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.gui.inspector.InspectionFrame.InspectionViewSettings;

import java.util.function.Consumer;
import java.util.function.Function;

class InspectionHistoryForwardAction implements InspectionAction
{
	private final History<InspectionViewSettings>			history;
	private final Function<InspectionViewSettings, String>	stringRepresentationProvider;
	private final Runnable									updateHistoryEntryRunnable;
	private final Consumer<InspectionViewSettings>			nextSettingsConsumer;

	InspectionHistoryForwardAction(History<InspectionViewSettings> history, Function<InspectionViewSettings, String> stringRepresentationProvider, Runnable updateHistoryEntryRunnable, Consumer<InspectionViewSettings> nextSettingsConsumer) {
		this.history = history;
		this.stringRepresentationProvider = stringRepresentationProvider;
		this.updateHistoryEntryRunnable = updateHistoryEntryRunnable;
		this.nextSettingsConsumer = nextSettingsConsumer;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return "Forward";
	}

	@Override
	public String getDescription() {
		if (!isEnabled()) {
			return null;
		}
		InspectionViewSettings nextSettings = history.peekNextElement();
		return stringRepresentationProvider.apply(nextSettings);
	}

	@Override
	public boolean isEnabled() {
		return history.canGoForward();
	}

	@Override
	public void perform() {
		updateHistoryEntryRunnable.run();
		InspectionViewSettings nextSettings = history.peekNextElement();
		history.goForward();
		nextSettingsConsumer.accept(nextSettings);
	}
}
