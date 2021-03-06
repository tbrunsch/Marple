package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.MapSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;
import dd.kms.zenodot.api.wrappers.TypeInfo;

class MapSettingsPanel extends AbstractMapSettingsPanel
{
	MapSettingsPanel(TypeInfo commonElementType, InspectionContext context) {
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
