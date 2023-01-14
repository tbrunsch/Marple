package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.CountOperationExecutor;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.CountSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;

class CountSettingsPanel extends AbstractMapSettingsPanel
{
	CountSettingsPanel(Class<?> commonElementType, InspectionContext context) {
		super(CountOperationExecutor.FUNCTIONAL_INTERFACE, commonElementType, context);
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
