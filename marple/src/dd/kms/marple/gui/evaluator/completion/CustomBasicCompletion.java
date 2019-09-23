package dd.kms.marple.gui.evaluator.completion;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.IntRange;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

import javax.swing.text.JTextComponent;

class CustomBasicCompletion extends BasicCompletion implements CustomCompletion
{
	private final CompletionSuggestion suggestion;

	CustomBasicCompletion(CompletionSuggestion suggestion, int relevance, CompletionProvider completionProvider) {
		super(completionProvider, suggestion.getTextToInsert(), suggestion.getType().toString());

		this.suggestion = suggestion;

		setRelevance(relevance);
		setIcon(IconFactory.getIcon(suggestion));
	}

	@Override
	public int compareTo(Completion completion) {
		return Integer.compare(getRelevance(), completion.getRelevance());
	}

	@Override
	public String getAlreadyEntered(JTextComponent textComponent) {
		String text = textComponent.getText();
		IntRange insertionRange = suggestion.getInsertionRange();
		return text.substring(insertionRange.getBegin(), insertionRange.getEnd());
	}

	@Override
	public CompletionSuggestion getCompletionSuggestion() {
		return suggestion;
	}
}
