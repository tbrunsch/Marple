package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.impl.inspector.InspectionHistory;

public class HistoryActionWrapper implements InspectionAction
{
	private final InspectionHistory history;
	private final InspectionAction	wrappedAction;

	public HistoryActionWrapper(InspectionHistory history, InspectionAction wrappedAction) {
		this.history = history;
		this.wrappedAction = wrappedAction;
	}

	@Override
	public boolean isDefaultAction() {
		return wrappedAction.isDefaultAction();
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
