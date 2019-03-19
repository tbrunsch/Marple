package dd.kms.marple.swing;

import java.awt.event.KeyEvent;

class SwingKeyboardShortCut
{
	private final int	keyCode;
	private final int	modifiers;

	SwingKeyboardShortCut(int keyCode, int modifiers) {
		this.keyCode = keyCode;
		this.modifiers = modifiers;
	}

	boolean matches(KeyEvent e) {
		return e.getKeyCode() == keyCode
				&& (e.getModifiers() & modifiers) == modifiers;
	}
}
