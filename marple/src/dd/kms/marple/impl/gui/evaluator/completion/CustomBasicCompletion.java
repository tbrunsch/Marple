package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.zenodot.api.result.CodeCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

import javax.swing.text.JTextComponent;

class CustomBasicCompletion extends BasicCompletion implements CustomCompletion
{
	private final CodeCompletion	codeCompletion;

	CustomBasicCompletion(CodeCompletion codeCompletion, int relevance, CompletionProvider completionProvider) {
		super(completionProvider, codeCompletion.getTextToInsert(), codeCompletion.getType().toString());

		this.codeCompletion = codeCompletion;

		setRelevance(relevance);
		setIcon(IconFactory.getIcon(codeCompletion));
	}

	@Override
	public int compareTo(Completion completion) {
		return CompletionsFactory.COMPLETION_COMPARATOR.compare(this, completion);
	}

	@Override
	public String getAlreadyEntered(JTextComponent textComponent) {
		String text = textComponent.getText();
		return text.substring(codeCompletion.getInsertionBegin(), codeCompletion.getInsertionEnd());
	}

	@Override
	public CodeCompletion getCodeCompletion() {
		return codeCompletion;
	}

	@Override
	public String toString() {
		return codeCompletion.toString();
	}
}
