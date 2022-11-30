package dd.kms.marple.impl.gui.common;

import java.util.ArrayList;
import java.util.List;

public class History<T>
{
	private final List<T>	historyElements				= new ArrayList<>();
	private int				historyPosition;
	private int				lastValidHistoryPosition;

	public History() {
		clear();
		checkInvariants();
	}

	public boolean hasElements() {
		return !historyElements.isEmpty();
	}

	public T get() {
		assert hasElements();
		return historyElements.get(historyPosition);
	}

	public void set(T element) {
		assert hasElements();
		historyElements.set(historyPosition, element);
	}

	public void add(T element) {
		if (historyPosition < historyElements.size() - 1) {
			historyElements.set(++historyPosition, element);
			lastValidHistoryPosition = historyPosition;
		} else {
			assert historyPosition == historyElements.size() - 1;
			assert lastValidHistoryPosition == historyPosition;
			historyElements.add(element);
			historyPosition++;
			lastValidHistoryPosition++;
		}
	}

	public boolean canGoBack() {
		return historyPosition > 0;
	}

	public T peekPreviousElement() {
		assert canGoBack();
		return historyElements.get(historyPosition-1);
	}

	public void goBack() {
		assert canGoBack();
		historyPosition--;
		assert historyPosition < lastValidHistoryPosition;
		checkInvariants();
	}

	public boolean canGoForward() {
		return historyPosition < lastValidHistoryPosition;
	}

	public T peekNextElement() {
		assert canGoForward();
		return historyElements.get(historyPosition+1);
	}

	public void goForward() {
		assert canGoForward();
		historyPosition++;
		assert historyPosition > 0;
		checkInvariants();
	}

	public void clear() {
		historyElements.clear();
		historyPosition = -1;
		lastValidHistoryPosition = -1;
	}

	private void checkInvariants() {
		assert -1 <= historyPosition && historyPosition < historyElements.size();
		assert -1 <= lastValidHistoryPosition && lastValidHistoryPosition < historyElements.size();
		assert historyPosition <= lastValidHistoryPosition;
	}
}
