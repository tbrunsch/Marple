package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.inspector.InspectionData;

public class InspectionHistoryForwardAction implements InspectionAction
{
	private final ObjectInspector			inspector;
	private final History<InspectionData>	history;

	public InspectionHistoryForwardAction(ObjectInspector inspector, History<InspectionData> history) {
		this.inspector = inspector;
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
		if (!isEnabled()) {
			return null;
		}
		InspectionData nextElement = history.peekNextElement();
		InspectionAction nextAction = nextElement.getAction();
		return nextAction.getDescription();
	}

	@Override
	public boolean isEnabled() {
		return history.canGoForward();
	}

	@Override
	public void perform() {
		InspectionHistoryUtils.storeViewSettings(inspector, history);
		InspectionData nextData = history.peekNextElement();
		history.goForward();
		InspectionHistoryUtils.restoreState(inspector, nextData);
	}
}
