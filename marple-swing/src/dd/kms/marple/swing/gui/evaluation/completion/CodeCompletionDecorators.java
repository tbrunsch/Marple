package dd.kms.marple.swing.gui.evaluation.completion;

import dd.kms.marple.swing.SwingKey;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.result.CompletionSuggestion;

import javax.swing.text.JTextComponent;
import java.util.List;

public class CodeCompletionDecorators
{
	public static void decorate(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, SwingKey completionSuggestionKey, ExpressionConsumer expressionConsumer) {
		new CodeCompletionDecorator(textComponent, completionSuggestionProvider, completionSuggestionKey, expressionConsumer);
	}

	@FunctionalInterface
	public interface CompletionSuggestionProvider
	{
		List<CompletionSuggestion> suggestCompletions(String expression, int caretPosition) throws ParseException;
	}

	@FunctionalInterface
	public interface ExpressionConsumer
	{
		void consume(String expression);
	}
}
