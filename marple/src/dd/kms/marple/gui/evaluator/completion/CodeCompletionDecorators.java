package dd.kms.marple.gui.evaluator.completion;

import dd.kms.marple.gui.common.GuiCommons;
import dd.kms.marple.settings.keys.KeyRepresentation;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class CodeCompletionDecorators
{
	private static final KeyRepresentation	APPLY_KEY	= new KeyRepresentation(0, KeyEvent.VK_ENTER);

	public static void decorate(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, KeyRepresentation completionSuggestionKey,
			@Nullable ExecutableArgumentInfoProvider executableArgumentInfoProvider, @Nullable KeyRepresentation showExecutableArgumentsKey,
			@Nullable Consumer<String> inputConsumer, @Nullable Consumer<Throwable> exceptionConsumer) {
		CompletionProvider provider = new CodeCompletionProvider(completionSuggestionProvider, exceptionConsumer);

		if (exceptionConsumer != null) {
			/*
			 * The auto completion library does not request completions when, among others, removing characters.
			 * However, we need instant feedback about parse exceptions.
			 */
			registerExceptionConsumer(textComponent, completionSuggestionProvider, exceptionConsumer);
		}

		Runnable onShowExecutableArguments = () -> showExecutableArguments(textComponent, executableArgumentInfoProvider);

		AutoCompletion ac = new CustomAutoCompletion(provider, completionSuggestionKey, onShowExecutableArguments);
		ac.install(textComponent);

		if (showExecutableArgumentsKey != null && executableArgumentInfoProvider != null) {
			GuiCommons.installKeyHandler(textComponent, showExecutableArgumentsKey, "Display method argument info", onShowExecutableArguments);
		}
		if (inputConsumer != null) {
			GuiCommons.installKeyHandler(textComponent, APPLY_KEY, "Evaluate result", () -> evaluateInput(textComponent, inputConsumer));
		}
	}

	private static void registerExceptionConsumer(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, Consumer<Throwable> exceptionConsumer) {
		textComponent.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkParseException();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkParseException();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				checkParseException();
			}

			private void checkParseException() {
				SwingUtilities.invokeLater(() -> {
					String text = textComponent.getText();
					int caretPosition = textComponent.getCaretPosition();
					try {
						completionSuggestionProvider.provideRatedSuggestions(text, caretPosition);
						exceptionConsumer.accept(null);
					} catch (Throwable t) {
						exceptionConsumer.accept(t);
					}
				});
			}
		});
	}

	private static void showExecutableArguments(JTextComponent textComponent, ExecutableArgumentInfoProvider executableArgumentInfoProvider)  {
		if (textComponent.getComponentPopupMenu() instanceof ExecutableArgumentPopup) {
			return;
		}
		ExecutableArgumentPopup.register(textComponent, executableArgumentInfoProvider);
	}

	private static void evaluateInput(JTextComponent textComponent, Consumer<String> inputConsumer) {
		inputConsumer.accept(textComponent.getText());
	}

	public static void configureExceptionComponent(JComponent exceptionComponent) {
		GuiCommons.setFontStyle(exceptionComponent, Font.PLAIN);
		exceptionComponent.setForeground(Color.RED);
	}

}
