package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.gui.inspector.InspectionFrame;
import dd.kms.marple.impl.inspector.InspectionData;
import dd.kms.marple.impl.inspector.ObjectInspectorImpl;

class InspectionHistoryUtils
{
	static void storeViewSettings(ObjectInspector inspector, History<InspectionData> history) {
		if (!history.hasElements()) {
			return;
		}
		InspectionFrame inspectionFrame = getInspectionFrame(inspector);
		if (inspectionFrame == null) {
			return;
		}
		Object viewSettings = inspectionFrame.getViewSettings();
		InspectionData oldHistoryData = history.get();
		InspectionData newHistoryData = oldHistoryData.replaceViewSettings(viewSettings);
		history.set(newHistoryData);
	}

	public static void restoreState(ObjectInspector inspector, InspectionData data) {
		InspectionAction action = data.getAction();
		action.perform();
		Object viewSettings = data.getViewSettings();
		if (viewSettings == null) {
			return;
		}
		InspectionFrame inspectionFrame = getInspectionFrame(inspector);
		if (inspectionFrame == null) {
			return;
		}
		inspectionFrame.applyViewSettings(viewSettings, ObjectView.ViewSettingsOrigin.SAME_CONTEXT);
	}

	private static InspectionFrame getInspectionFrame(ObjectInspector inspector) {
		return inspector instanceof ObjectInspectorImpl
				? ((ObjectInspectorImpl) inspector).getInspectionFrame()
				: null;
	}
}
