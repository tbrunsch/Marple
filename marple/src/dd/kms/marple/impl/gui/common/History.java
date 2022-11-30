package dd.kms.marple.impl.gui.common;

import java.util.ArrayList;
import java.util.List;

public class History<T>
{
	private final List<T>	historyElements				= new ArrayList<>();
	private int				historyPosition;

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
			reduceHistorySize(historyPosition + 1);
		} else {
			assert historyPosition == historyElements.size() - 1;
			historyElements.add(element);
			historyPosition++;
		}
	}

	public boolean canGoBack() {
		return historyPosition > 0;
	}

	public T peekPreviousElement() {
		assert canGoBack();
		return historyElements.get(historyPosition - 1);
	}

	public void goBack() {
		assert canGoBack();
		historyPosition--;
		checkInvariants();
	}

	public boolean canGoForward() {
		return historyPosition + 1 < historyElements.size();
	}

	public T peekNextElement() {
		assert canGoForward();
		return historyElements.get(historyPosition + 1);
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
	}

	private void checkInvariants() {
		assert -1 <= historyPosition && historyPosition < historyElements.size();
	}

	private void reduceHistorySize(int maxSize) {
		while (historyElements.size() > maxSize) {
			historyElements.remove(historyElements.size() - 1);
		}
	}
}
