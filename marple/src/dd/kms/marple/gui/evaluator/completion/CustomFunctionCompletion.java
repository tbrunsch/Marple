package dd.kms.marple.gui.evaluator.completion;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.IntRange;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionMethod;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;

class CustomFunctionCompletion extends FunctionCompletion implements CustomCompletion
{
	private final CompletionSuggestion	suggestion;
	private final ExecutableInfo		methodInfo;

	CustomFunctionCompletion(CompletionSuggestionMethod suggestion, int relevance, CompletionProvider completionProvider) {
		super(completionProvider, suggestion.getMethodInfo().getName(), suggestion.getMethodInfo().getReturnType().toString());

		this.suggestion = suggestion;
		this.methodInfo = suggestion.getMethodInfo();

		List<Parameter> params = new ArrayList<>();
		int numArguments = methodInfo.getNumberOfArguments();
		for (int i = 0; i < numArguments; i++) {
			TypeInfo type = methodInfo.getExpectedArgumentType(i);
			String name = "arg" + i;
			ParameterizedCompletion.Parameter param = new ParameterizedCompletion.Parameter(type, name, i == numArguments - 1);
			params.add(param);
		}
		setShortDescription(suggestion.getType().toString());
		setParams(params);
		setRelevance(relevance);
		setReturnValueDescription(methodInfo.getReturnType().getSimpleName());
		setShortDescription(suggestion.getType().toString());
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
	public String getReplacementText() {
		return suggestion.getTextToInsert();
	}

	@Override
	public String toString() {
		return suggestion.toString();
	}

	@Override
	public CompletionSuggestion getCompletionSuggestion() {
		return suggestion;
	}
}
