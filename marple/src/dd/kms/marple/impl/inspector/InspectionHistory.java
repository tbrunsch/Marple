package dd.kms.marple.impl.inspector;

import dd.kms.marple.api.actions.InspectionAction;

import java.util.ArrayList;
import java.util.List;

public class InspectionHistory
{
	private final List<InspectionAction>	history						= new ArrayList<>();
	private int								historyPosition;
	private int								lastValidHistoryPosition;

	public InspectionHistory() {
		clear();
		checkInvariants();
	}

	public void addToHistory(InspectionAction action) {
		if (historyPosition < history.size() - 1) {
			history.set(++historyPosition, action);
			lastValidHistoryPosition = historyPosition;
		} else {
			assert historyPosition == history.size() - 1;
			assert lastValidHistoryPosition == historyPosition;
			history.add(action);
			historyPosition++;
			lastValidHistoryPosition++;
		}
	}

	public boolean canGoBack() {
		return historyPosition > 0;
	}

	public InspectionAction getPreviousAction() {
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

	public InspectionAction getNextAction() {
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
