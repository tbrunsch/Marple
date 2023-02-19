package dd.kms.marple.impl.gui.help;

import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.keys.KeyRepresentation;

import java.util.List;

class CustomActionKeysPanel extends AbstractKeysPanel
{
	CustomActionKeysPanel(CustomActionSettings customActionSettings) {
		super("Keys for custom actions");

		List<CustomAction> customActions = customActionSettings.getCustomActions();
		boolean foundCustomActionWithKey = false;
		for (CustomAction customAction : customActions) {
			KeyRepresentation key = customAction.getKey();
			if (key != null) {
				foundCustomActionWithKey = true;
				addKeyDescription(customAction.getName(),	key);
			}
		}

		setVisible(foundCustomActionWithKey);
	}
}
