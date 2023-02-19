package dd.kms.marple.api.settings.actions;

import com.google.common.collect.ImmutableList;

import java.util.List;

public interface CustomActionSettings
{
	static CustomActionSettings of(List<CustomAction> customActions) {
		return new dd.kms.marple.impl.settings.actions.CustomActionSettingsImpl(customActions);
	}

	static CustomActionSettings of(CustomAction... customActions) {
		return of(ImmutableList.copyOf(customActions));
	}

	List<CustomAction> getCustomActions();
	void setCustomActions(List<CustomAction> customActions);
}
