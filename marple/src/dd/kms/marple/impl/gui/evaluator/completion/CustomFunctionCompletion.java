package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.IntRange;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionMethod;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;

class CustomFunctionCompletion extends FunctionCompletion implements CustomCompletion
{
	private final CodeCompletion	completion;
	private final ExecutableInfo	methodInfo;

	CustomFunctionCompletion(CodeCompletionMethod completion, int relevance, CompletionProvider completionProvider) {
		super(completionProvider, completion.getMethodInfo().getName(), completion.getMethodInfo().getReturnType().toString());

		this.completion = completion;
		this.methodInfo = completion.getMethodInfo();

		List<Parameter> params = new ArrayList<>();
		int numArguments = methodInfo.getNumberOfArguments();
		for (int i = 0; i < numArguments; i++) {
			TypeInfo type = methodInfo.getExpectedArgumentType(i);
			String name = "arg" + i;
			ParameterizedCompletion.Parameter param = new ParameterizedCompletion.Parameter(type, name, i == numArguments - 1);
			params.add(param);
		}
		setShortDescription(completion.getType().toString());
		setParams(params);
		setRelevance(relevance);
		setReturnValueDescription(methodInfo.getReturnType().getSimpleName());
		setShortDescription(completion.getType().toString());
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
	public String getReplacementText() {
		return completion.getTextToInsert();
	}

	@Override
	public String toString() {
		return completion.toString();
	}

	@Override
	public CodeCompletion getCodeCompletion() {
		return completion;
	}
}
