package dd.kms.marple.actions;

import dd.kms.marple.inspector.InspectionHistory;

public class HistoryBackAction implements InspectionAction
{
	private final InspectionHistory	history;

	public HistoryBackAction(InspectionHistory history) {
		this.history = history;
	}

	@Override
	public String getName() {
		return "Back";
	}

	@Override
	public String getDescription() {
		return isEnabled() ? history.getPreviousAction().getDescription() : null;
	}

	@Override
	public boolean isEnabled() {
		return history.canGoBack();
	}

	@Override
	public void perform() {
		InspectionAction prevAction = history.getPreviousAction();
		history.goBack();
		prevAction.perform();
	}
}
