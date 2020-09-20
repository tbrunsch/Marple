package dd.kms.marple.impl.settings;

import dd.kms.marple.api.settings.SecuritySettings;

class NoSecuritySettings implements SecuritySettings
{
	static final SecuritySettings INSTANCE = new NoSecuritySettings();

	/* Singleton */
	private NoSecuritySettings() {}

	@Override
	public String getPasswordHash() {
		return null;
	}

	@Override
	public String hashPassword(String password) {
		return null;
	}

	@Override
	public String queryPassword() {
		return null;
	}
}
