package dd.kms.marple.gui.evaluator.completion;

import dd.kms.marple.settings.KeyRepresentation;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CodeCompletionDecorators
{
	public static void decorate(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, KeyRepresentation completionSuggestionKey, ExecutableArgumentInfoProvider executableArgumentInfoProvider, KeyRepresentation showExecutableArgumentsKey, Consumer<String> expressionConsumer) {
		new CodeCompletionDecorator(textComponent, completionSuggestionProvider, completionSuggestionKey, executableArgumentInfoProvider, showExecutableArgumentsKey, expressionConsumer);
	}

	public static void configureExceptionComponent(JComponent exceptionComponent) {
		exceptionComponent.setFont(exceptionComponent.getFont().deriveFont(Font.PLAIN));
		exceptionComponent.setForeground(Color.RED);
	}

	@FunctionalInterface
	public interface CompletionSuggestionProvider
	{
		List<CompletionSuggestion> suggestCompletions(String expression, int caretPosition) throws ParseException;
	}

	@FunctionalInterface
	public interface ExecutableArgumentInfoProvider
	{
		Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String expression, int caretPosition) throws ParseException;
	}
}
