package dd.kms.marple.settings;

public class KeyRepresentation
{
	private final int	keyCode;
	private final int	modifiers;

	public KeyRepresentation(int keyCode, int modifiers) {
		this.keyCode = keyCode;
		this.modifiers = modifiers;
	}

	public boolean matches(KeyRepresentation expectedKey) {
		return expectedKey != null && keyCode == expectedKey.keyCode && modifiers == expectedKey.modifiers;
	}
}
