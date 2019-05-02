package dd.kms.marple.swing.gui.evaluation.completion;

import dd.kms.marple.swing.SwingKey;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;

import javax.swing.text.JTextComponent;
import java.util.List;
import java.util.Optional;

public class CodeCompletionDecorators
{
	public static void decorate(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, SwingKey completionSuggestionKey, ExecutableArgumentInfoProvider executableArgumentInfoProvider, SwingKey showExecutableArgumentsKey, ExpressionConsumer expressionConsumer) {
		new CodeCompletionDecorator(textComponent, completionSuggestionProvider, completionSuggestionKey, executableArgumentInfoProvider, showExecutableArgumentsKey, expressionConsumer);
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

	@FunctionalInterface
	public interface ExpressionConsumer
	{
		void consume(String expression);
	}
}
