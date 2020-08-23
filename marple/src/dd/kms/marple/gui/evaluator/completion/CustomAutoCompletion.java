package dd.kms.marple.gui.evaluator.completion;

import dd.kms.marple.settings.keys.KeyRepresentation;
import dd.kms.zenodot.api.result.CodeCompletion;
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
			CodeCompletion codeCompletion = customCompletion.getCodeCompletion();
			JTextComponent textComponent = getTextComponent();
			int caretPositionAfterInsertion = codeCompletion.getCaretPositionAfterInsertion();
			textComponent.setCaretPosition(caretPositionAfterInsertion);
		}
		if (completion instanceof CustomFunctionCompletion) {
			onShowExecutableArguments.run();
		}
	}
}
