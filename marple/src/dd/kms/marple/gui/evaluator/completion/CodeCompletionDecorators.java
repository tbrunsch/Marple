package dd.kms.marple.gui.evaluator.completion;

import dd.kms.marple.gui.common.GuiCommons;
import dd.kms.marple.settings.keys.KeyRepresentation;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class CodeCompletionDecorators
{
	private static final KeyRepresentation	APPLY_KEY	= new KeyRepresentation(0, KeyEvent.VK_ENTER);

	public static void decorate(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, KeyRepresentation completionSuggestionKey) {
		decorate(textComponent, completionSuggestionProvider, completionSuggestionKey, null, null, null, null);
	}

	public static void decorate(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, KeyRepresentation completionSuggestionKey,
			@Nullable ExecutableArgumentInfoProvider executableArgumentInfoProvider, @Nullable KeyRepresentation showExecutableArgumentsKey,
			@Nullable Consumer<String> inputConsumer, @Nullable Consumer<Throwable> exceptionConsumer) {
		CompletionProvider provider = new CodeCompletionProvider(completionSuggestionProvider, exceptionConsumer);

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
