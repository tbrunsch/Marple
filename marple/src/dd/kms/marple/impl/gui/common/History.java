package dd.kms.marple.impl.gui.common;

import java.util.ArrayList;
import java.util.List;

public class History<T>
{
	private final List<T>	history						= new ArrayList<>();
	private int				historyPosition;
	private int				lastValidHistoryPosition;

	public History() {
		clear();
		checkInvariants();
	}

	public boolean hasElements() {
		return !history.isEmpty();
	}

	public T get() {
		assert hasElements();
		return history.get(historyPosition);
	}

	public void set(T element) {
		assert hasElements();
		history.set(historyPosition, element);
	}

	public void add(T element) {
		if (historyPosition < history.size() - 1) {
			history.set(++historyPosition, element);
			lastValidHistoryPosition = historyPosition;
		} else {
			assert historyPosition == history.size() - 1;
			assert lastValidHistoryPosition == historyPosition;
			history.add(element);
			historyPosition++;
			lastValidHistoryPosition++;
		}
	}

	public boolean canGoBack() {
		return historyPosition > 0;
	}

	public T peekPreviousElement() {
		assert canGoBack();
		return history.get(historyPosition-1);
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
		return history.get(historyPosition+1);
	}

	public void goForward() {
		assert canGoForward();
		historyPosition++;
		assert historyPosition > 0;
		checkInvariants();
	}

	public void clear() {
		history.clear();
		historyPosition = -1;
		lastValidHistoryPosition = -1;
	}

	private void checkInvariants() {
		assert -1 <= historyPosition && historyPosition < history.size();
		assert -1 <= lastValidHistoryPosition && lastValidHistoryPosition < history.size();
		assert historyPosition <= lastValidHistoryPosition;
	}
}
