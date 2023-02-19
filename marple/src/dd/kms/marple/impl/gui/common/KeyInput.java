package dd.kms.marple.impl.gui.common;

import com.google.common.collect.ImmutableSet;
import dd.kms.marple.api.settings.keys.KeyRepresentation;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;
import java.util.function.Consumer;

public class KeyInput extends JTextField implements KeyListener
{
	private static final KeyRepresentation	ESCAPE		= new KeyRepresentation(0, KeyEvent.VK_ESCAPE);

	private static final Set<Integer>	IGNORED_KEY_CODES	= ImmutableSet.of(0,
		KeyEvent.VK_ENTER, KeyEvent.VK_BACK_SPACE, KeyEvent.VK_TAB, KeyEvent.VK_CANCEL,
		KeyEvent.VK_CLEAR, KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL, KeyEvent.VK_ALT,
		KeyEvent.VK_PAUSE, KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_ESCAPE, KeyEvent.VK_SPACE,
		KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_END, KeyEvent.VK_HOME,
		KeyEvent.VK_LEFT, KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN
	);

	private final Consumer<KeyRepresentation>	keyListener;

	private boolean								waitingForKey;
	private int									modifiers;
	private int									keyCode;

	public KeyInput(Consumer<KeyRepresentation> keyListener) {
		super("Press any key combination");

		setEditable(false);

		this.keyListener = keyListener;

		addKeyListener(this);
	}

	public void waitForKey() {
		modifiers = 0;
		keyCode = 0;
		waitingForKey = true;
	}

	private void addToKeyRepresentation(KeyEvent e) {
		modifiers |= e.getModifiers();
		int keyCode = e.getKeyCode();
		if (!IGNORED_KEY_CODES.contains(keyCode)) {
			this.keyCode = keyCode;
		}
	}

	private void finishEnteringKey() {
		if (!waitingForKey) {
			return;
		}
		KeyRepresentation pressedKey = new KeyRepresentation(modifiers, keyCode);
		if (!pressedKey.matches(ESCAPE)) {
			keyListener.accept(pressedKey);
		}
		waitingForKey = false;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* nothing to do */
	}

	@Override
	public void keyPressed(KeyEvent e) {
		addToKeyRepresentation(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (IGNORED_KEY_CODES.contains(keyCode)) {
			modifiers = 0;
			keyCode = 0;
		} else {
			finishEnteringKey();
		}
	}
}
