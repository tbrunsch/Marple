package dd.kms.marple.api.settings;

public interface SecuritySettings
{
	/**
	 * Returns the hash of the expected password
	 */
	String getPasswordHash();

	/**
	 * Creates the hash of a password
	 */
	String hashPassword(String password);

	/**
	 * Queries the password and returns the result
	 */
	String queryPassword();
}
