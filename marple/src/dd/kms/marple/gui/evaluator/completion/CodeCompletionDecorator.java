package dd.kms.marple.gui.evaluator.completion;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators.CompletionSuggestionProvider;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators.ExecutableArgumentInfoProvider;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators.ExpressionConsumer;
import dd.kms.marple.settings.KeyRepresentation;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.result.IntRange;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class CodeCompletionDecorator
{
	private static final int				MAX_NUM_SUGGESTIONS	= 20;
	private static final KeyRepresentation	APPLY_KEY			= new KeyRepresentation(KeyEvent.VK_ENTER, 0);

	private final JTextComponent					textComponent;

	private JPopupMenu								popupMenu;

	private final CompletionSuggestionProvider		completionSuggestionProvider;
	private final KeyRepresentation					completionSuggestionKey;
	private final ExecutableArgumentInfoProvider	executableArgumentInfoProvider;
	private final KeyRepresentation					showExecutableArgumentsKey;
	private final ExpressionConsumer				expressionConsumer;

	private DisplayMode								displayMode						= DisplayMode.NOTHING;

	CodeCompletionDecorator(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, KeyRepresentation completionSuggestionKey, ExecutableArgumentInfoProvider executableArgumentInfoProvider, KeyRepresentation showExecutableArgumentsKey, ExpressionConsumer expressionConsumer) {
		this.textComponent = textComponent;
		this.completionSuggestionProvider = completionSuggestionProvider;
		this.completionSuggestionKey = completionSuggestionKey;
		this.executableArgumentInfoProvider = executableArgumentInfoProvider;
		this.showExecutableArgumentsKey = showExecutableArgumentsKey;
		this.expressionConsumer = expressionConsumer;

		textComponent.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});

		textComponent.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateCompletionSuggestions();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateCompletionSuggestions();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateCompletionSuggestions();
			}

			private void updateCompletionSuggestions() {
				SwingUtilities.invokeLater(() -> updatePopupMenu());
			}
		});

		textComponent.addCaretListener(e -> SwingUtilities.invokeLater(() -> updatePopupMenu()));
	}

	private void updatePopupMenu() {
		switch (displayMode) {
			case EXECUTABLE_ARGUMENTS:
				showExecutableArguments();
				break;
			default:
				showSuggestions();
		}
	}

	private void showSuggestions() {
		displayMode = DisplayMode.COMPLETION_SUGGESTIONS;
		if (popupMenuExists()) {
			setPopupMenuCodeCompletionActions();
			if (popupMenu.getComponentCount() == 0) {
				destroyPopupMenu();
			}
			return;
		}

		createPopupMenu();
		setPopupMenuCodeCompletionActions();
		if (popupMenu.getComponentCount() == 0) {
			destroyPopupMenu();
		} else {
			showPopupMenu();
		}
	}

	private void showExecutableArguments() {
		displayMode = DisplayMode.EXECUTABLE_ARGUMENTS;
		if (popupMenuExists()) {
			setPopupMenuExecutableArguments();
			if (popupMenu.getComponentCount() == 0) {
				destroyPopupMenu();
			}
			return;
		}

		createPopupMenu();
		setPopupMenuExecutableArguments();
		if (popupMenu.getComponentCount() == 0) {
			destroyPopupMenu();
		} else {
			showPopupMenu();
		}
	}

	private boolean popupMenuExists() {
		return popupMenu != null && popupMenu.isVisible();
	}

	private void showPopupMenu() {
		popupMenu.setBorder(BorderFactory.createEtchedBorder());
		popupMenu.show(textComponent, 0, textComponent.getHeight());
		popupMenu.setVisible(true);
		textComponent.requestFocus();
	}

	private void createPopupMenu() {
		popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				/* do nothing */
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				SwingUtilities.invokeLater(CodeCompletionDecorator.this::destroyPopupMenu);
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				destroyPopupMenu();
			}
		});
	}

	private void destroyPopupMenu() {
		if (popupMenu != null) {
			popupMenu.setVisible(false);
			popupMenu = null;
		}
		displayMode = DisplayMode.NOTHING;
	}

	private void setPopupMenuCodeCompletionActions() {
		popupMenu.removeAll();
		List<Action> actions = ImmutableList.of();
		try {
			actions = getCompletionActions(textComponent.getText(), textComponent.getCaretPosition());
		} catch (ParseException e) {
			JMenuItem menuItem = new JMenuItem(CodeCompletionDecorators.formatExceptionMessage(textComponent.getText(), e));
			CodeCompletionDecorators.configureExceptionComponent(menuItem);
			popupMenu.add(menuItem);
		}
		for (Action action : actions) {
			JMenuItem menuItem = new JMenuItem(action);
			menuItem.setBackground(Color.WHITE);
			popupMenu.add(menuItem);
		}
		popupMenu.pack();
	}

	private List<Action> getCompletionActions(String expression, int caretPosition) throws ParseException {
		List<CompletionSuggestion> completionSuggestions = completionSuggestionProvider.suggestCompletions(expression, caretPosition);
		ImmutableList.Builder<Action> actionsBuilder = ImmutableList.builder();
		for (int i = 0; i < Math.min(completionSuggestions.size(), MAX_NUM_SUGGESTIONS); i++) {
			CompletionSuggestion completionSuggestion = completionSuggestions.get(i);
			Action action = createCompletionSuggestionAction(expression, completionSuggestion);
			actionsBuilder.add(action);
		}
		return actionsBuilder.build();
	}

	private Action createCompletionSuggestionAction(String expression, CompletionSuggestion completionSuggestion) {
		int caretPositionAfterInsertion = completionSuggestion.getCaretPositionAfterInsertion();
		IntRange insertionRange = completionSuggestion.getInsertionRange();
		String textToInsert = completionSuggestion.getTextToInsert();
		return new AbstractAction(completionSuggestion.toString()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = expression.substring(0, insertionRange.getBegin())
								+ textToInsert
								+ expression.substring(insertionRange.getEnd());
				textComponent.setText(text);
				textComponent.setCaretPosition(caretPositionAfterInsertion);
			}
		};
	}

	private void setPopupMenuExecutableArguments() {
		popupMenu.removeAll();
		List<JMenuItem> executableArgumentInfoMenuItems;
		try {
			executableArgumentInfoMenuItems = getExecutableArgumentInfoMenuItems(textComponent.getText(), textComponent.getCaretPosition());
		} catch (ParseException e) {
			JMenuItem menuItem = new JMenuItem(CodeCompletionDecorators.formatExceptionMessage(textComponent.getText(), e));
			CodeCompletionDecorators.configureExceptionComponent(menuItem);
			executableArgumentInfoMenuItems = ImmutableList.of(menuItem);
		}
		for (JMenuItem menuItem : executableArgumentInfoMenuItems) {
			popupMenu.add(menuItem);
		}
		popupMenu.pack();
	}

	private List<JMenuItem> getExecutableArgumentInfoMenuItems(String expression, int caretPosition) throws ParseException {
		Optional<ExecutableArgumentInfo> executableArgumentInfo = executableArgumentInfoProvider.getExecutableArgumentInfo(expression, caretPosition);
		if (!executableArgumentInfo.isPresent()) {
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

	private void applySelectedAction() {
		JMenuItem selectedMenuItem = getSelectedMenuItem();
		if (selectedMenuItem != null) {
			Action action = selectedMenuItem.getAction();
			if (action != null) {
				action.actionPerformed(new ActionEvent(popupMenu, ActionEvent.ACTION_PERFORMED, null));
			}
		}
		destroyPopupMenu();
	}

	private JMenuItem getSelectedMenuItem() {
		MenuElement[] selectedPath = MenuSelectionManager.defaultManager().getSelectedPath();
		if (selectedPath == null || selectedPath.length == 0) {
			return null;
		}
		MenuElement selectedMenuElement = selectedPath[selectedPath.length - 1];
		return selectedMenuElement instanceof JMenuItem ? (JMenuItem) selectedMenuElement : null;
	}

	/*
	 * Listeners
	 */
	void handleKeyPressed(KeyEvent e) {
		KeyRepresentation key = new KeyRepresentation(e.getKeyCode(), e.getModifiers());
		if (key.matches(completionSuggestionKey)) {
			showSuggestions();
			return;
		}
		if (key.matches(showExecutableArgumentsKey)) {
			showExecutableArguments();
			return;
		}
		if (key.matches(APPLY_KEY)) {
			if (popupMenuExists()) {
				applySelectedAction();
			} else {
				expressionConsumer.consume(textComponent.getText());
			}
			return;
		}
	}

	private enum DisplayMode { NOTHING, COMPLETION_SUGGESTIONS, EXECUTABLE_ARGUMENTS };
}
