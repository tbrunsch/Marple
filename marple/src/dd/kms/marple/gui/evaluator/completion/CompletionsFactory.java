package dd.kms.marple.gui.evaluator.completion;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionMethod;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class CompletionsFactory
{
	private static final int NUM_COMPLETION_TYPES = CodeCompletionType.values().length;

	static int getRelevance(CodeCompletion completion) {
		int rating = completion.getRating().getRatingValue();
		return rating * NUM_COMPLETION_TYPES + (NUM_COMPLETION_TYPES - 1 - completion.getType().ordinal());
	}

	private final CompletionProvider			completionProvider;
	private final CompletionSuggestionProvider	suggestionProvider;
	private final Consumer<Throwable>			exceptionConsumer;

	CompletionsFactory(CompletionProvider completionProvider, CompletionSuggestionProvider suggestionProvider, Consumer<Throwable> exceptionConsumer) {
		this.completionProvider = completionProvider;
		this.suggestionProvider = suggestionProvider;
		this.exceptionConsumer = exceptionConsumer;
	}

	List<Completion> getCompletions(String text, int caretPosition) {
		List<CodeCompletion> codeCompletions = getCodeCompletions(text, caretPosition);
		List<Completion> completions = codeCompletions.stream()
			.map(this::createCompletion)
			.collect(Collectors.toList());
		Collections.sort(completions,
			Comparator.comparing(Completion::getRelevance)
				.reversed()
				.thenComparing(completion -> completion.toString().toLowerCase()));
		return completions;
	}

	List<ParameterizedCompletion> getParameterizedCompletions(String text, int caretPosition) {
		List<CodeCompletion> completions = getCodeCompletions(text, caretPosition);
		return completions.stream()
			.filter(completion -> completion.getType() == CodeCompletionType.METHOD)
			.sorted(Comparator.comparingInt(CompletionsFactory::getRelevance))
			.map(completion -> createParameterizedCompletion((CodeCompletionMethod) completion))
			.collect(Collectors.toList());
	}

	private List<CodeCompletion> getCodeCompletions(String text, int caretPosition) {
		List<CodeCompletion> completions;
		Throwable throwable = null;
		try {
			completions = suggestionProvider.provideCodeCompletions(text, caretPosition);
		} catch (Throwable t) {
			throwable = t;
			completions = ImmutableList.of();
		}
		if (exceptionConsumer != null) {
			exceptionConsumer.accept(throwable);
		}
		return completions;
	}

	private Completion createCompletion(CodeCompletion completion) {
		if (completion.getType() == CodeCompletionType.METHOD) {
			return createParameterizedCompletion((CodeCompletionMethod) completion);
		}
		int relevance = getRelevance(completion);
		return new CustomBasicCompletion(completion, relevance, completionProvider);
	}

	private ParameterizedCompletion createParameterizedCompletion(CodeCompletionMethod completion) {
		int relevance = getRelevance(completion);
		return new CustomFunctionCompletion(completion, relevance, completionProvider);
	}
}
