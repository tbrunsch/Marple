package dd.kms.marple.gui.evaluator.completion;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ExecutableArgumentPopup extends JPopupMenu
{
	private static int DELTA_Y	= -5;

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

		caretListener = new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				updatePopup();
			}
		};
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
		Rectangle r;
		try {
			r = textComponent.modelToView(textComponent.getCaretPosition());
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
		Optional<ExecutableArgumentInfo> executableArgumentInfo = executableArgumentInfoProvider.getExecutableArgumentInfo(text, caretPosition);
		if (!executableArgumentInfo.isPresent()) {
			unregister();
			return ImmutableList.of();
		}
		ExecutableArgumentInfo info = executableArgumentInfo.get();
		int currentArgIndex = info.getCurrentArgumentIndex();
		Map<ExecutableInfo, Boolean> applicableExecutableOverloads = info.getApplicableExecutableOverloads();

		ImmutableList.Builder<JMenuItem> menuItemBuilder = ImmutableList.builder();
		for (ExecutableInfo executableInfo : applicableExecutableOverloads.keySet()) {
			boolean applicable = applicableExecutableOverloads.get(executableInfo);
			StringBuilder argumentInfoTextBuilder = new StringBuilder();
			argumentInfoTextBuilder.append("<html>").append(executableInfo.getName()).append("(");
			int numArguments = executableInfo.getNumberOfArguments();
			for (int argIndex = 0; argIndex < numArguments; argIndex++) {
				String argTypeAsString = executableInfo.getExpectedArgumentType(argIndex).getRawType().getSimpleName();
				if (executableInfo.isVariadic() && argIndex == numArguments - 1) {
					argTypeAsString += "...";
				}
				boolean highlight = false;
				if (applicable) {
					if (executableInfo.isVariadic()) {
						highlight = argIndex <= currentArgIndex;
					} else {
						highlight = argIndex == currentArgIndex;
					}
				}
				if (argIndex > 0) {
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
