package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.marple.api.evaluator.StringHistory;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.framework.common.UniformDocumentListener;
import dd.kms.marple.impl.gui.common.GuiCommons;
import org.fife.ui.autocomplete.AutoCompletion;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;

public class CodeCompletionDecorator
{
	private static final KeyRepresentation	APPLY_KEY	= new KeyRepresentation(0, KeyEvent.VK_ENTER);
	private static final KeyRepresentation	UP_KEY		= new KeyRepresentation(0, KeyEvent.VK_UP);
	private static final KeyRepresentation	DOWN_KEY	= new KeyRepresentation(0, KeyEvent.VK_DOWN);

	private final JTextComponent	textComponent;
	private final ParserMediator	parserMediator;
	private final KeyRepresentation	completionSuggestionKey;
	@Nullable
	private final KeyRepresentation	showExecutableArgumentsKey;
	private final StringHistory		expressionHistory;

	public CodeCompletionDecorator(JTextComponent textComponent, ParserMediator parserMediator, KeyRepresentation completionSuggestionKey, @Nullable KeyRepresentation showExecutableArgumentsKey, StringHistory expressionHistory) {
		this.textComponent = textComponent;
		this.parserMediator = parserMediator;
		this.completionSuggestionKey = completionSuggestionKey;
		this.showExecutableArgumentsKey = showExecutableArgumentsKey;
		this.expressionHistory = expressionHistory;
	}

	public void decorate() {
		/*
		 * The auto completion library does not request completions when, among others, removing characters.
		 * However, we need instant feedback about parse exceptions.
		 */
		registerExceptionConsumer();

		Runnable onShowExecutableArguments = this::showExecutableArguments;

		AutoCompletion ac = new CustomAutoCompletion(parserMediator, completionSuggestionKey, onShowExecutableArguments);
		ac.install(textComponent);

		if (showExecutableArgumentsKey != null) {
			GuiCommons.installKeyHandler(textComponent, showExecutableArgumentsKey, "Display method argument info", onShowExecutableArguments);
		}
		GuiCommons.installKeyHandler(textComponent, APPLY_KEY,	"Evaluate result",			this::evaluateInput);
		GuiCommons.installKeyHandler(textComponent, UP_KEY,		"Show previous expression", () -> lookUpInExpressionHistory(StringHistory.LookupDirection.PREVIOUS));
		GuiCommons.installKeyHandler(textComponent, DOWN_KEY,	"Show next expression",		() -> lookUpInExpressionHistory(StringHistory.LookupDirection.NEXT));
	}

	private void registerExceptionConsumer() {
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

	private void showExecutableArguments()  {
		if (textComponent.getComponentPopupMenu() instanceof ExecutableArgumentPopup) {
			return;
		}
		ExecutableArgumentPopup.register(textComponent, parserMediator);
	}

	public void evaluateInput() {
		String expression = textComponent.getText();
		expressionHistory.addString(expression);
		parserMediator.consumeText(expression);
	}

	private void lookUpInExpressionHistory(StringHistory.LookupDirection lookupDirection) {
		String expression = expressionHistory.lookup(lookupDirection);
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
