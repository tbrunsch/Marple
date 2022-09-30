package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.MapSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;

class MapSettingsPanel extends AbstractMapSettingsPanel
{
	MapSettingsPanel(Class<?> commonElementType, InspectionContext context) {
		super(commonElementType, context);
	}

	@Override
	OperationSettings getSettings() {
		String mappingExpression = getMappingExpression();
		return new MapSettings(mappingExpression);
	}

	@Override
	void setSettings(OperationSettings settings) {
		MapSettings mapSettings = (MapSettings) settings;
		setMappingExpression(mapSettings.getMappingExpression());
	}
}
