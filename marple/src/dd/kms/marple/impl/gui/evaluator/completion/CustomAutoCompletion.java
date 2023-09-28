package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.zenodot.api.common.ClassInfo;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionClass;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;

import javax.swing.text.JTextComponent;

class CustomAutoCompletion extends AutoCompletion
{
	private final ParserMediator	parserMediator;
	private final Runnable			onShowExecutableArguments;

	CustomAutoCompletion(ParserMediator parserMediator, KeyRepresentation completionSuggestionKey, Runnable onShowExecutableArguments) {
		super(new CodeCompletionProvider(parserMediator));

		this.parserMediator = parserMediator;
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
		if (codeCompletion.getType() == CodeCompletionType.CLASS) {
			CodeCompletionClass classCompletion = (CodeCompletionClass) codeCompletion;
			if (classCompletion.isQualifiedCompletion()) {
				CodeCompletionClass unqualifiedCompletion = classCompletion.asUnqualifiedCompletion();
				String unqualifiedClassName = unqualifiedCompletion.getTextToInsert();
				if (!parserMediator.isClassImported(unqualifiedClassName)) {
					Class<?> clazz;
					try {
						ClassInfo classInfo = classCompletion.getClassInfo();
						clazz = classInfo.asClass();
					} catch (IllegalStateException e) {
						parserMediator.consumeException(e);
						return;
					}
					parserMediator.importClassTemporarily(clazz);
					codeCompletion = classCompletion.asUnqualifiedCompletion();
				}
			}
		}
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
