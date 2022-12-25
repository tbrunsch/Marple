package dd.kms.marple.impl.gui.evaluator.textfields;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.impl.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.settings.ParserSettings;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractInputTextField<T> extends JTextField
{
	static List<CodeCompletion> filterCompletions(List<CodeCompletion> completions) {
		return completions.stream()
			.filter(completion -> completion.getRating().getNameMatch() != StringMatch.NONE)
			.collect(Collectors.toList());
	}

	private final InspectionContext		context;

	private Consumer<T>					evaluationResultConsumer	= result -> {};
	private Consumer<Throwable>			exceptionConsumer			= e -> {};

	/*
	 * Cached Data
	 */
	private String						cachedText					= null;
	private int							cachedCaretPosition			= -1;
	private List<CodeCompletion>		cachedRatedSuggestions		= ImmutableList.of();
	private ParseException				cachedParseException		= null;

	AbstractInputTextField(InspectionContext context) {
		this.context = context;

		KeySettings keySettings = context.getSettings().getKeySettings();
		CodeCompletionDecorators.decorate(
			this,
			this::provideCodeCompletions,
			keySettings.getCodeCompletionKey(),
			this::getExecutableArgumentInfo,
			keySettings.getShowMethodArgumentsKey(),
			this::consumeText,
			this::consumeException
		);
	}

	abstract List<CodeCompletion> doProvideCompletions(String text, int caretPosition) throws ParseException;
	abstract Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException;
	abstract T evaluate(String text) throws ParseException;

	public void setEvaluationResultConsumer(Consumer<T> evaluationResultConsumer) {
		this.evaluationResultConsumer = evaluationResultConsumer;
	}

	/**
	 * The exception consumer will also be called with {@code null} to notify the consumer
	 * that currently there is no exception.
	 */
	public void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
		this.exceptionConsumer = exceptionConsumer;
	}

	public void addInputVerifier() {
		setInputVerifier(new ParserVerifier());
	}

	public T evaluateText() throws ParseException {
		return evaluate(getText());
	}

	ParserSettings getParserSettings() {
		return context.getEvaluator().getParserSettings();
	}

	List<Variable> getVariables() {
		return context.getEvaluator().getVariables();
	}

	void setVariables(List<Variable> variables) {
		context.getEvaluator().setVariables(variables);
	}

	private List<CodeCompletion> provideCodeCompletions(String text, int caretPosition) throws ParseException {
		if (!Objects.equals(text, cachedText) || caretPosition != cachedCaretPosition) {
			cachedText = text;
			cachedCaretPosition = caretPosition;
			try {
				cachedRatedSuggestions = doProvideCompletions(text, caretPosition);
				cachedParseException = null;
			} catch (ParseException e) {
				cachedParseException = e;
			}
		}
		if (cachedParseException != null) {
			throw cachedParseException;
		}
		return cachedRatedSuggestions;
	}

	private void consumeText(String text) {
		try {
			T evaluationResult = evaluate(text);
			evaluationResultConsumer.accept(evaluationResult);
		} catch (Throwable t) {
			consumeException(t);
		}
	}

	private void consumeException(Throwable t) {
		exceptionConsumer.accept(t);
	}

	private class ParserVerifier extends InputVerifier
	{
		@Override
		public boolean verify(JComponent input) {
			if (!(input instanceof JTextComponent)) {
				return false;
			}
			JTextComponent textInput = (JTextComponent) input;
			String text = textInput.getText();
			if (Strings.isNullOrEmpty(text)) {
				// allow leaving the text component if no text is entered
				return true;
			}
			try {
				evaluate(text);
				return true;
			} catch (ParseException e) {
				return false;
			}
		}
	}
}
