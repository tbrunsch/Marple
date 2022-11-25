package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.inspector.InspectionData;

public class InspectionHistoryBackAction implements InspectionAction
{
	private final ObjectInspector			inspector;
	private final History<InspectionData>	history;

	public InspectionHistoryBackAction(ObjectInspector inspector, History<InspectionData> history) {
		this.inspector = inspector;
		this.history = history;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return "Back";
	}

	@Override
	public String getDescription() {
		if (!isEnabled()) {
			return null;
		}
		InspectionData previousElement = history.peekPreviousElement();
		InspectionAction previousAction = previousElement.getAction();
		return previousAction.getDescription();
	}

	@Override
	public boolean isEnabled() {
		return history.canGoBack();
	}

	@Override
	public void perform() {
		InspectionHistoryUtils.storeViewSettings(inspector, history);
		InspectionData prevData = history.peekPreviousElement();
		history.goBack();
		InspectionHistoryUtils.restoreState(inspector, prevData);
	}
}
