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

	public void set(T element) {
		assert !historyElements.isEmpty();
		historyElements.set(historyPosition, element);
	}

	public void add(T element) {
		reduceHistorySize(historyPosition + 1);
		assert historyPosition == historyElements.size() - 1;
		historyElements.add(element);
		historyPosition++;
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
