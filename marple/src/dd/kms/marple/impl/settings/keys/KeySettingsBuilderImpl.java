package dd.kms.marple.impl.settings.keys;

import dd.kms.marple.api.settings.keys.KeyFunction;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.api.settings.keys.KeySettingsBuilder;

import java.util.HashMap;
import java.util.Map;

public class KeySettingsBuilderImpl implements KeySettingsBuilder
{
	private final Map<KeyFunction, KeyRepresentation>	keys	= new HashMap<>();

	public KeySettingsBuilderImpl() {
		for (KeyFunction function : KeyFunction.values()) {
			keys.put(function, function.getDefaultKey());
		}
	}

	@Override
	public KeySettingsBuilder key(KeyFunction function, KeyRepresentation key) {
		keys.put(function, key);
		return this;
	}

	@Override
	public KeySettings build() {
		return new KeySettingsImpl(keys);
	}
}
