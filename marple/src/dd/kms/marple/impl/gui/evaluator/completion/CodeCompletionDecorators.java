package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.framework.common.UniformDocumentListener;
import dd.kms.marple.impl.gui.common.GuiCommons;
import org.fife.ui.autocomplete.AutoCompletion;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;

public class CodeCompletionDecorators
{
	private static final KeyRepresentation	APPLY_KEY	= new KeyRepresentation(0, KeyEvent.VK_ENTER);
	private static final KeyRepresentation	UP_KEY		= new KeyRepresentation(0, KeyEvent.VK_UP);
	private static final KeyRepresentation	DOWN_KEY	= new KeyRepresentation(0, KeyEvent.VK_DOWN);

	public static void decorate(JTextComponent textComponent, ParserMediator parserMediator, KeyRepresentation completionSuggestionKey,
			@Nullable KeyRepresentation showExecutableArgumentsKey) {
		/*
		 * The auto completion library does not request completions when, among others, removing characters.
		 * However, we need instant feedback about parse exceptions.
		 */
		registerExceptionConsumer(textComponent, parserMediator);

		Runnable onShowExecutableArguments = () -> showExecutableArguments(textComponent, parserMediator);

		AutoCompletion ac = new CustomAutoCompletion(parserMediator, completionSuggestionKey, onShowExecutableArguments);
		ac.install(textComponent);

		if (showExecutableArgumentsKey != null) {
			GuiCommons.installKeyHandler(textComponent, showExecutableArgumentsKey, "Display method argument info", onShowExecutableArguments);
		}
		ExpressionHistory expressionHistory = new ExpressionHistory();
		GuiCommons.installKeyHandler(textComponent, APPLY_KEY, "Evaluate result", () -> evaluateInput(textComponent, parserMediator, expressionHistory));
		GuiCommons.installKeyHandler(textComponent, UP_KEY, "Show previous expression", () -> lookUpInExpressionHistory(textComponent, expressionHistory, ExpressionHistory.PREVIOUS));
		GuiCommons.installKeyHandler(textComponent, DOWN_KEY, "Show next expression", () -> lookUpInExpressionHistory(textComponent, expressionHistory, ExpressionHistory.NEXT));
	}

	private static void registerExceptionConsumer(JTextComponent textComponent, ParserMediator parserMediator) {
		textComponent.getDocument().addDocumentListener(new UniformDocumentListener() {
			@Override
			protected void onDocumentChanged() {
				SwingUtilities.invokeLater(() -> {
					String text = textComponent.getText();
					int caretPosition = textComponent.getCaretPosition();
					if (!GuiCommons.isCaretPositionValid(text, caretPosition)) {
						return;
					}
					try {
						parserMediator.provideCodeCompletions(text, caretPosition);
						parserMediator.consumeException(null);
					} catch (Throwable t) {
						parserMediator.consumeException(t);
					}
				});
			}
		});
	}

	private static void showExecutableArguments(JTextComponent textComponent, ParserMediator parserMediator)  {
		if (textComponent.getComponentPopupMenu() instanceof ExecutableArgumentPopup) {
			return;
		}
		ExecutableArgumentPopup.register(textComponent, parserMediator);
	}

	private static void evaluateInput(JTextComponent textComponent, ParserMediator parserMediator, ExpressionHistory expressionHistory) {
		String expression = textComponent.getText();
		expressionHistory.addExpression(expression);
		parserMediator.consumeText(expression);
	}

	private static void lookUpInExpressionHistory(JTextComponent textComponent, ExpressionHistory expressionHistory, int searchDirection) {
		String expression = expressionHistory.lookup(searchDirection);
		if (expression != null) {
			textComponent.setText(expression);
			textComponent.setCaretPosition(expression.length());
		}
	}

	public static void configureExceptionComponent(JComponent exceptionComponent) {
		GuiCommons.setFontStyle(exceptionComponent, Font.PLAIN);
		exceptionComponent.setForeground(Color.RED);
	}

}
