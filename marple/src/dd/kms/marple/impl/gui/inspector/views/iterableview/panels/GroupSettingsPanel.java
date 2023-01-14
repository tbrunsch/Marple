package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.GroupOperationExecutor;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.GroupSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;

class GroupSettingsPanel extends AbstractMapSettingsPanel
{
	GroupSettingsPanel(Class<?> commonElementType, InspectionContext context) {
		super(GroupOperationExecutor.FUNCTIONAL_INTERFACE, commonElementType, context);
	}

	@Override
	OperationSettings getSettings() {
		String mappingExpression = getMappingExpression();
		return new GroupSettings(mappingExpression);
	}

	@Override
	void setSettings(OperationSettings settings) {
		GroupSettings groupSettings = (GroupSettings) settings;
		setMappingExpression(groupSettings.getMappingExpression());
	}
}
