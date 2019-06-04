package dd.kms.marple.actions.history;

import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.inspector.InspectionHistory;

public class HistoryForwardAction implements InspectionAction
{
	private final InspectionHistory	history;

	public HistoryForwardAction(InspectionHistory history) {
		this.history = history;
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
		return history.canGoForward() ? history.getNextAction().getDescription() : null;
	}

	@Override
	public boolean isEnabled() {
		return history.canGoForward();
	}

	@Override
	public void perform() {
		InspectionAction nextAction = history.getNextAction();
		history.goForward();
		nextAction.perform();
	}
}
