package dd.kms.marple.gui.evaluator.completion;

import dd.kms.marple.settings.keys.KeyRepresentation;
import dd.kms.zenodot.result.CompletionSuggestion;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

import javax.swing.text.JTextComponent;

class CustomAutoCompletion extends AutoCompletion
{
	private final Runnable onShowExecutableArguments;

	CustomAutoCompletion(CompletionProvider provider, KeyRepresentation completionSuggestionKey, Runnable onShowExecutableArguments) {
		super(provider);

		this.onShowExecutableArguments = onShowExecutableArguments;

		setAutoActivationEnabled(true);
		setAutoActivationDelay(0);
		setTriggerKey(completionSuggestionKey.asKeyStroke());
	}

	@Override
	protected void insertCompletion(Completion completion, boolean typedParamListStartChar) {
		super.insertCompletion(completion, typedParamListStartChar);
		if (completion instanceof CustomCompletion) {
			CustomCompletion customCompletion = (CustomCompletion) completion;
			CompletionSuggestion completionSuggestion = customCompletion.getCompletionSuggestion();
			JTextComponent textComponent = getTextComponent();
			int caretPositionAfterInsertion = completionSuggestion.getCaretPositionAfterInsertion();
			textComponent.setCaretPosition(caretPositionAfterInsertion);
		}
		if (completion instanceof CustomFunctionCompletion) {
			onShowExecutableArguments.run();
		}
	}
}
