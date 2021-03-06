package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.GroupSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;
import dd.kms.zenodot.api.wrappers.TypeInfo;

class GroupSettingsPanel extends AbstractMapSettingsPanel
{
	GroupSettingsPanel(TypeInfo commonElementType, InspectionContext context) {
		super(commonElementType, context);
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
