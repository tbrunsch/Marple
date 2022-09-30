package dd.kms.marple.impl.gui.evaluator.completion;

import com.google.common.collect.Range;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionMethod;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import javax.swing.text.JTextComponent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class CustomFunctionCompletion extends FunctionCompletion implements CustomCompletion
{
	private final CodeCompletion	completion;
	private final Method			method;

	CustomFunctionCompletion(CodeCompletionMethod completion, int relevance, CompletionProvider completionProvider) {
		super(completionProvider, completion.getMethod().getName(), completion.getMethod().getReturnType().toString());

		this.completion = completion;
		this.method = completion.getMethod();

		List<Parameter> params = new ArrayList<>();
		Class<?>[] parameterTypes = method.getParameterTypes();
		int numParameters = parameterTypes.length;
		for (int i = 0; i < numParameters; i++) {
			Class<?> type = parameterTypes[i];
			String name = "arg" + i;
			ParameterizedCompletion.Parameter param = new ParameterizedCompletion.Parameter(type, name, i == numParameters - 1);
			params.add(param);
		}
		setShortDescription(completion.getType().toString());
		setParams(params);
		setRelevance(relevance);
		setReturnValueDescription(method.getReturnType().getSimpleName());
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
		Range<Integer> insertionRange = completion.getInsertionRange();
		return text.substring(insertionRange.lowerEndpoint(), insertionRange.upperEndpoint());
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
