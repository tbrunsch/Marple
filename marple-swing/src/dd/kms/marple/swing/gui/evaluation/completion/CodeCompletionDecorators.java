package dd.kms.marple.swing.gui.evaluation.completion;

import dd.kms.marple.swing.SwingKey;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class CodeCompletionDecorators
{
	public static void decorate(JTextComponent textComponent, CompletionSuggestionProvider completionSuggestionProvider, SwingKey completionSuggestionKey, ExecutableArgumentInfoProvider executableArgumentInfoProvider, SwingKey showExecutableArgumentsKey, ExpressionConsumer expressionConsumer) {
		new CodeCompletionDecorator(textComponent, completionSuggestionProvider, completionSuggestionKey, executableArgumentInfoProvider, showExecutableArgumentsKey, expressionConsumer);
	}

	public static String formatExceptionMessage(String expression, ParseException e) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><p><b>").append(e.getMessage().replace("\n", "<br/>")).append("</b></p>");
		Throwable cause = e.getCause();
		if (cause != null) {
			builder.append("<br/>").append(formatException(cause));
		}
		int position = e.getPosition();
		if (0 <= position && position < expression.length()) {
			builder.append("<br/>").append(expression.substring(0, position)).append('^').append(expression.substring(position));
		}
		builder.append("</html>");
		return builder.toString();
	}

	private static String formatException(Throwable e) {
		StringBuilder builder = new StringBuilder();
		builder.append("<p>").append(e.getClass().getSimpleName());
		String message = e.getMessage();
		if (message != null) {
			builder.append(": ").append(message);
		}
		Throwable cause = e.getCause();
		if (cause != null) {
			builder.append("<br/>  Cause: ").append(formatException(cause));
		}
		builder.append("</p>");
		return builder.toString();
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

	@FunctionalInterface
	public interface ExpressionConsumer
	{
		void consume(String expression);
	}
}
