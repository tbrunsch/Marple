package dd.kms.marple.gui.evaluator.textfields;

import com.google.common.base.Strings;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.marple.settings.keys.KeySettings;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractInputTextField<T> extends JTextField
{
	private final InspectionContext		inspectionContext;

	private Consumer<T>					evaluationResultConsumer	= result -> {};
	private Consumer<Throwable>			exceptionConsumer			= e -> {};

	AbstractInputTextField(InspectionContext inspectionContext) {
		this.inspectionContext = inspectionContext;

		KeySettings keySettings = inspectionContext.getSettings().getKeySettings();
		CodeCompletionDecorators.decorate(
			this,
			this::provideRatedSuggestions,
			keySettings.getCodeCompletionKey(),
			this::getExecutableArgumentInfo,
			keySettings.getShowMethodArgumentsKey(),
			this::consumeText,
			this::consumeException
		);
	}

	abstract Map<CompletionSuggestion, Integer> provideRatedSuggestions(String text, int caretPosition) throws ParseException;
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
		return inspectionContext.getEvaluator().getParserSettings();
	}

	private void consumeText(String text) {
		try {
			T evaluationResult = evaluate(text);
			evaluationResultConsumer.accept(evaluationResult);
			consumeException(null);
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
