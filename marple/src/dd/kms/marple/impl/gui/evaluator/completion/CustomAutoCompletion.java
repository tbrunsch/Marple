package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionClass;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

import javax.annotation.Nullable;
import javax.swing.text.JTextComponent;
import java.util.function.Consumer;

class CustomAutoCompletion extends AutoCompletion
{
	private final Runnable onShowExecutableArguments;

	CustomAutoCompletion(ParserMediator parserMediator, KeyRepresentation completionSuggestionKey, Runnable onShowExecutableArguments) {
		super(new CodeCompletionProvider(parserMediator));

		this.onShowExecutableArguments = onShowExecutableArguments;

		setAutoActivationEnabled(true);
		setAutoActivationDelay(0);
		setTriggerKey(completionSuggestionKey.asKeyStroke());
	}

	@Override
	protected void insertCompletion(Completion completion, boolean typedParamListStartChar) {
		/*
		 * Do not call super.insertCompletion(completion, typedParamListStartChar) because
		 * this method can only replace text until the caret.
		 */
		hidePopupWindow();
		if (!(completion instanceof CustomCompletion)) {
			throw new UnsupportedOperationException("Unsupported code completion class: " + completion.getClass());
		}
		CustomCompletion customCompletion = (CustomCompletion) completion;
		CodeCompletion codeCompletion = customCompletion.getCodeCompletion();
		JTextComponent textComponent = getTextComponent();
		String text = textComponent.getText();

		StringBuilder builder = new StringBuilder();
		int insertionBegin = codeCompletion.getInsertionBegin();
		if (insertionBegin > 0) {
			builder.append(text, 0, insertionBegin);
		}
		builder.append(codeCompletion.getTextToInsert());
		int insertionEnd = codeCompletion.getInsertionEnd();
		if (insertionEnd < text.length()) {
			builder.append(text.substring(insertionEnd));
		}
		textComponent.setCaretPosition(0);	// must reset caret to avoid "invalid caret" exception when setting text
		textComponent.setText(builder.toString());

		int caretPositionAfterInsertion = codeCompletion.getCaretPositionAfterInsertion();
		textComponent.setCaretPosition(caretPositionAfterInsertion);

		if (completion instanceof CustomFunctionCompletion) {
			onShowExecutableArguments.run();
		}
	}
}
