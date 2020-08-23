package dd.kms.marple.gui.evaluator.completion;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.IntRange;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

import javax.swing.text.JTextComponent;

class CustomBasicCompletion extends BasicCompletion implements CustomCompletion
{
	private final CodeCompletion	completion;

	CustomBasicCompletion(CodeCompletion completion, int relevance, CompletionProvider completionProvider) {
		super(completionProvider, completion.getTextToInsert(), completion.getType().toString());

		this.completion = completion;

		setRelevance(relevance);
		setIcon(IconFactory.getIcon(completion));
	}

	@Override
	public int compareTo(Completion completion) {
		return Integer.compare(getRelevance(), completion.getRelevance());
	}

	@Override
	public String getAlreadyEntered(JTextComponent textComponent) {
		String text = textComponent.getText();
		IntRange insertionRange = completion.getInsertionRange();
		return text.substring(insertionRange.getBegin(), insertionRange.getEnd());
	}

	@Override
	public CodeCompletion getCodeCompletion() {
		return completion;
	}

	@Override
	public String toString() {
		return completion.toString();
	}
}
