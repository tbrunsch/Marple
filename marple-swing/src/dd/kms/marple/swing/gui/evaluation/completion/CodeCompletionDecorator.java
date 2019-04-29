package dd.kms.marple.swing.gui.evaluation.completion;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.swing.SwingKey;
import dd.kms.marple.swing.gui.evaluation.completion.CodeCompletionDecorators.CompletionSuggestionProvider;
import dd.kms.marple.swing.gui.evaluation.completion.CodeCompletionDecorators.ExpressionConsumer;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.IntRange;

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

class CodeCompletionDecorator
{
	private static final int		MAX_NUM_SUGGESTIONS	= 20;
	private static final SwingKey 	APPLY_KEY			= new SwingKey(KeyEvent.VK_ENTER, 0);

	private final JTextComponent				textComponent;

	private JPopupMenu							popupMenu;

	private final CompletionSuggestionProvider	completionSuggestionProvider;
	private final SwingKey						completionSuggestionKey;
	private final ExpressionConsumer			expressionConsumer;

	CodeCompletionDecorator(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, SwingKey completionSuggestionKey, ExpressionConsumer expressionConsumer) {
		this.textComponent = textComponent;
		this.completionSuggestionProvider = completionSuggestionProvider;
		this.completionSuggestionKey = completionSuggestionKey;
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
				if (popupMenu == null || !popupMenu.isVisible()) {
					return;
				}
				showSuggestions();
			}
		});
	}

	private void showSuggestions() {
		if (popupMenuExists()) {
			setPopupMenuActions();
			if (popupMenu.getComponentCount() == 0) {
				destroyPopupMenu();
			}
			return;
		}

		createPopupMenu();
		setPopupMenuActions();
		if (popupMenu.getComponentCount() == 0) {
			destroyPopupMenu();
		} else {
			popupMenu.setBorder(BorderFactory.createEtchedBorder());
			popupMenu.show(textComponent, 0, textComponent.getHeight());
			popupMenu.setVisible(true);
			textComponent.requestFocus();
		}
	}

	private boolean popupMenuExists() {
		return popupMenu != null && popupMenu.isVisible();
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
	}

	private void setPopupMenuActions() {
		popupMenu.removeAll();
		List<Action> actions = ImmutableList.of();
		try {
			actions = getCompletionActions(textComponent.getText(), textComponent.getCaretPosition());
		} catch (ParseException e) {
			JMenuItem menuItem = new JMenuItem(e.getMessage());
			menuItem.setForeground(Color.RED);
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
		SwingKey key = new SwingKey(e.getKeyCode(), e.getModifiers());
		if (isRequestCodeCompletion(key)) {
			showSuggestions();
			return;
		}
		if (isApplySuggestion(key)) {
			if (popupMenuExists()) {
				applySelectedAction();
			} else {
				expressionConsumer.consume(textComponent.getText());
			}
			return;
		}
	}

	private boolean isRequestCodeCompletion(SwingKey key) {
		return key.matches(completionSuggestionKey);
	}

	private boolean isApplySuggestion(SwingKey key) {
		return key.matches(APPLY_KEY);
	}
}
