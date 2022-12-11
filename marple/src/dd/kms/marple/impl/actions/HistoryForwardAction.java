package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.gui.common.History;

import java.util.function.Consumer;
import java.util.function.Function;

public class HistoryForwardAction<T> implements InspectionAction
{
	private final History<T>			history;
	private final Function<T, String>	stringRepresentationProvider;
	private final Runnable				updateHistoryElementRunnable;
	private final Consumer<T>			historyElementConsumer;

	public HistoryForwardAction(History<T> history, Function<T, String> stringRepresentationProvider, Runnable updateHistoryElementRunnable, Consumer<T> historyElementConsumer) {
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
		return "Forward";
	}

	@Override
	public String getDescription() {
		if (!isEnabled()) {
			return null;
		}
		T nextElement = history.peekNextElement();
		return stringRepresentationProvider.apply(nextElement);
	}

	@Override
	public boolean isEnabled() {
		return history.canGoForward();
	}

	@Override
	public void perform() {
		updateHistoryElementRunnable.run();
		T nextElement = history.peekNextElement();
		history.goForward();
		historyElementConsumer.accept(nextElement);
	}
}
