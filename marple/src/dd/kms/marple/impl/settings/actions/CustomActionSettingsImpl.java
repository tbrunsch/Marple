package dd.kms.marple.impl.settings.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.actions.CustomActionSettings;

import java.util.List;

public class CustomActionSettingsImpl implements CustomActionSettings
{
	private List<CustomAction> customActions;

	public CustomActionSettingsImpl(List<CustomAction> customActions) {
		setCustomActions(customActions);
	}

	@Override
	public List<CustomAction> getCustomActions() {
		return customActions;
	}

	@Override
	public void setCustomActions(List<CustomAction> customActions) {
		this.customActions = ImmutableList.copyOf(customActions);
	}
}
