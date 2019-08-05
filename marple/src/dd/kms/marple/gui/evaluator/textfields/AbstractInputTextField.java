package dd.kms.marple.gui.evaluator.textfields;

import com.google.common.base.Strings;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.marple.settings.keys.KeySettings;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.settings.ParserSettings;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractInputTextField<T> extends JTextField
{
	private final InspectionContext		inspectionContext;

	private Consumer<T>					evaluationResultConsumer	= result -> {};
	private Consumer<ParseException>	exceptionConsumer			= e -> {};

	AbstractInputTextField(InspectionContext inspectionContext) {
		this.inspectionContext = inspectionContext;

		KeySettings keySettings = inspectionContext.getSettings().getKeySettings();
		CodeCompletionDecorators.decorate(
			this,
			this::suggestCodeCompletions,
			keySettings.getCodeCompletionKey(),
			this::getExecutableArgumentInfo,
			keySettings.getShowMethodArgumentsKey(),
			this::consumeText
		);
	}

	abstract List<CompletionSuggestion> suggestCodeCompletions(String text, int caretPosition) throws ParseException;
	abstract Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException;
	abstract T evaluate(String text) throws ParseException;

	public void setEvaluationResultConsumer(Consumer<T> evaluationResultConsumer) {
		this.evaluationResultConsumer = evaluationResultConsumer;
	}

	public void setExceptionConsumer(Consumer<ParseException> exceptionConsumer) {
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
		} catch (ParseException e) {
			exceptionConsumer.accept(e);
		}
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
