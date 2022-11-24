package dd.kms.marple.impl.gui.evaluator.completion;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ExecutableArgumentPopup extends JPopupMenu
{
	private static final int DELTA_Y	= -5;

	static void register(JTextComponent textComponent, ExecutableArgumentInfoProvider executableArgumentInfoProvider) {
		if (textComponent.getComponentPopupMenu() instanceof ExecutableArgumentPopup) {
			return;
		}
		ExecutableArgumentPopup popup = new ExecutableArgumentPopup(textComponent, executableArgumentInfoProvider);
		popup.register();
	}

	private final JTextComponent					textComponent;
	private final ExecutableArgumentInfoProvider	executableArgumentInfoProvider;

	private DocumentListener						documentListener;
	private CaretListener							caretListener;
	private FocusListener							focusListener;
	private KeyListener								keyListener;

	ExecutableArgumentPopup(JTextComponent textComponent, ExecutableArgumentInfoProvider executableArgumentInfoProvider) {
		this.textComponent = textComponent;
		this.executableArgumentInfoProvider = executableArgumentInfoProvider;
		setInvoker(textComponent);
		setFocusable(false);
	}

	private void register() {
		textComponent.setComponentPopupMenu(this);

		documentListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updatePopup();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updatePopup();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updatePopup();
			}
		};
		textComponent.getDocument().addDocumentListener(documentListener);

		caretListener = e -> updatePopup();
		textComponent.addCaretListener(caretListener);

		focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				unregister();
			}
		};
		textComponent.addFocusListener(focusListener);

		keyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					unregister();
				}
			}
		};
		textComponent.addKeyListener(keyListener);

		updatePopup();
	}

	private void unregister() {
		if (documentListener != null) {
			textComponent.getDocument().removeDocumentListener(documentListener);
		}
		if (caretListener != null) {
			textComponent.removeCaretListener(caretListener);
		}
		if (focusListener != null) {
			textComponent.removeFocusListener(focusListener);
		}
		if (keyListener != null) {
			textComponent.removeKeyListener(keyListener);
		}

		textComponent.setComponentPopupMenu(null);

		setVisible(false);
		removeAll();
	}

	private boolean isRegistered() {
		return textComponent.getComponentPopupMenu() == this;
	}

	private void updatePopup() {
		List<JMenuItem> menuItems;
		try {
			menuItems = getExecutableArgumentInfoMenuItems();
		} catch (ParseException e) {
			menuItems = ImmutableList.of();
		}

		if (!isRegistered()) {
			return;
		}

		removeAll();
		for (JMenuItem menuItem : menuItems) {
			add(menuItem);
		}

		pack();
		setVisible(true);
		updateLocation();
	}

	private void updateLocation() {
		String text = textComponent.getText();
		int caretPosition = textComponent.getCaretPosition();
		if (!GuiCommons.isCaretPositionValid(text, caretPosition)) {
			return;
		}
		Rectangle r;
		try {
			r = textComponent.modelToView(caretPosition);
		} catch (BadLocationException e) {
			return;
		}
		Point p = new Point(r.x, r.y);
		SwingUtilities.convertPointToScreen(p, textComponent);
		p.translate(0, -getHeight() + DELTA_Y);
		setLocation(p);
	}

	private List<JMenuItem> getExecutableArgumentInfoMenuItems() throws ParseException {
		String text = textComponent.getText();
		int caretPosition = textComponent.getCaretPosition();
		Optional<ExecutableArgumentInfo> executableArgumentInfo = GuiCommons.isCaretPositionValid(text, caretPosition)
			? executableArgumentInfoProvider.getExecutableArgumentInfo(text, caretPosition)
			: Optional.empty();
		if (!executableArgumentInfo.isPresent()) {
			unregister();
			return ImmutableList.of();
		}
		ExecutableArgumentInfo info = executableArgumentInfo.get();
		int currentArgIndex = info.getCurrentArgumentIndex();
		Map<Executable, Boolean> applicableExecutableOverloads = info.getApplicableExecutableOverloads();

		ImmutableList.Builder<JMenuItem> menuItemBuilder = ImmutableList.builder();
		for (Executable executable : applicableExecutableOverloads.keySet()) {
			boolean applicable = applicableExecutableOverloads.get(executable);
			StringBuilder argumentInfoTextBuilder = new StringBuilder();
			argumentInfoTextBuilder.append("<html>").append(executable.getName()).append("(");
			Class<?>[] parameterTypes = executable.getParameterTypes();
			int numParameters = parameterTypes.length;
			for (int paramIndex = 0; paramIndex < numParameters; paramIndex++) {
				String argTypeAsString = parameterTypes[paramIndex].getSimpleName();
				if (executable.isVarArgs() && paramIndex == numParameters - 1) {
					argTypeAsString += "...";
				}
				boolean highlight = false;
				if (applicable) {
					if (executable.isVarArgs()) {
						highlight = paramIndex <= currentArgIndex;
					} else {
						highlight = paramIndex == currentArgIndex;
					}
				}
				if (paramIndex > 0) {
					argumentInfoTextBuilder.append(", ");
				}
				if (highlight) {
					argumentInfoTextBuilder.append("<b>").append(argTypeAsString).append("</b>");
				} else {
					argumentInfoTextBuilder.append(argTypeAsString);
				}
			}
			argumentInfoTextBuilder.append(")").append("</html>");

			JMenuItem menuItem = new JMenuItem(argumentInfoTextBuilder.toString());
			menuItem.setFont(menuItem.getFont().deriveFont(Font.PLAIN));

			Color fg = applicable ? Color.BLACK : Color.LIGHT_GRAY;
			menuItem.setForeground(fg);
			menuItem.setBackground(Color.WHITE);

			menuItemBuilder.add(menuItem);
		}
		return menuItemBuilder.build();
	}
}
