package dd.kms.marple.impl.gui.inspector;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.gui.inspector.InspectionFrame.InspectionViewSettings;

import java.util.function.Consumer;

class InspectionHistoryForwardAction implements InspectionAction
{
	private final InspectionContext					context;
	private final History<InspectionViewSettings>	history;
	private final Runnable							updateHistoryEntryRunnable;
	private final Consumer<InspectionViewSettings>	nextSettingsConsumer;

	InspectionHistoryForwardAction(InspectionContext context, History<InspectionViewSettings> history, Runnable updateHistoryEntryRunnable, Consumer<InspectionViewSettings> nextSettingsConsumer) {
		this.context = context;
		this.history = history;
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
		Object currentObject = nextSettings.getCurrentObject();
		return context.getDisplayText(currentObject);
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
