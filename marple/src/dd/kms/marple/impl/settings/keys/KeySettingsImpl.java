package dd.kms.marple.impl.settings.keys;

import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.settings.keys.KeyFunction;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.api.settings.keys.KeySettings;

import java.util.Map;

class KeySettingsImpl implements KeySettings
{
	private final Map<KeyFunction,	KeyRepresentation>	keys;

	KeySettingsImpl(Map<KeyFunction, KeyRepresentation> keys) {
		this.keys = ImmutableMap.copyOf(keys);
	}

	@Override
	public KeyRepresentation getKey(KeyFunction function) {
		return keys.get(function);
	}
}
