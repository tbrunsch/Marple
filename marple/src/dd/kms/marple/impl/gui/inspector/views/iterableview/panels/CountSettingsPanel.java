package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.CountSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;
import dd.kms.zenodot.api.wrappers.TypeInfo;

class CountSettingsPanel extends AbstractMapSettingsPanel
{
	CountSettingsPanel(TypeInfo commonElementType, InspectionContext context) {
		super(commonElementType, context);
	}

	@Override
	OperationSettings getSettings() {
		String mappingExpression = getMappingExpression();
		return new CountSettings(mappingExpression);
	}

	@Override
	void setSettings(OperationSettings settings) {
		CountSettings countSettings = (CountSettings) settings;
		setMappingExpression(countSettings.getMappingExpression());
	}
}
