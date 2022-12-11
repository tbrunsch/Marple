package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.common.History;

import java.util.function.Consumer;
import java.util.function.Function;

public class HistoryBackAction<T> implements InspectionAction
{
	private final History<T>			history;
	private final Function<T, String>	stringRepresentationProvider;
	private final Runnable				updateHistoryElementRunnable;
	private final Consumer<T>			historyElementConsumer;

	public HistoryBackAction(History<T> history, Function<T, String> stringRepresentationProvider, Runnable updateHistoryElementRunnable, Consumer<T> historyElementConsumer) {
		this.history = history;
		this.stringRepresentationProvider = stringRepresentationProvider;
		this.updateHistoryElementRunnable = updateHistoryElementRunnable;
		this.historyElementConsumer = historyElementConsumer;
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
		T prevElement = history.peekPreviousElement();
		return stringRepresentationProvider.apply(prevElement);
	}

	@Override
	public boolean isEnabled() {
		return history.canGoBack();
	}

	@Override
	public void perform() {
		updateHistoryElementRunnable.run();
		T prevElement = history.peekPreviousElement();
		history.goBack();
		historyElementConsumer.accept(prevElement);
	}
}
