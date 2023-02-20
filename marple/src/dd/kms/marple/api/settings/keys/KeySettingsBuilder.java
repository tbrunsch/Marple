package dd.kms.marple.api.settings.keys;

public interface KeySettingsBuilder
{
	static KeySettingsBuilder create() {
		return new dd.kms.marple.impl.settings.keys.KeySettingsBuilderImpl();
	}

	KeySettingsBuilder key(KeyFunction function, KeyRepresentation key);
	KeySettings build();
}
