package dd.kms.marple.actions;

import dd.kms.marple.InspectionHistory;

public class HistoryActionWrapper implements InspectionAction
{
	private final InspectionHistory history;
	private final InspectionAction	wrappedAction;

	public HistoryActionWrapper(InspectionHistory history, InspectionAction wrappedAction) {
		this.history = history;
		this.wrappedAction = wrappedAction;
	}

	@Override
	public String getName() {
		return wrappedAction.getName();
	}

	@Override
	public String getDescription() {
		return wrappedAction.getDescription();
	}

	@Override
	public boolean isEnabled() {
		return wrappedAction.isEnabled();
	}

	@Override
	public void perform() {
		history.addToHistory(wrappedAction);
		wrappedAction.perform();
	}
}
