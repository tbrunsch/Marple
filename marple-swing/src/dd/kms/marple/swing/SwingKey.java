package dd.kms.marple.swing;

public class SwingKey
{
	private final int	keyCode;
	private final int	modifiers;

	public SwingKey(int keyCode, int modifiers) {
		this.keyCode = keyCode;
		this.modifiers = modifiers;
	}

	public boolean matches(SwingKey expectedKey) {
		return keyCode == expectedKey.keyCode && modifiers == expectedKey.modifiers;
	}
}
