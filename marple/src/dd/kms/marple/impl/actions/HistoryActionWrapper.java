package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.inspector.InspectionData;

public class HistoryActionWrapper implements InspectionAction
{
	private final ObjectInspector			inspector;
	private final History<InspectionData>	history;
	private final InspectionAction			wrappedAction;

	public HistoryActionWrapper(ObjectInspector inspector, History<InspectionData> history, InspectionAction wrappedAction) {
		this.inspector = inspector;
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
		HistoryUtils.storeViewSettings(inspector, history);
		history.add(new InspectionData(wrappedAction, null));
		wrappedAction.perform();
	}
}
