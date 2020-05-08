package dd.kms.marple.gui.evaluator.completion;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestionType;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionMethod;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class CompletionsFactory
{
	private static final int NUM_SUGGESTION_TYPES = CompletionSuggestionType.values().length;

	static int getRelevance(CompletionSuggestion suggestion, int rating) {
		return rating * NUM_SUGGESTION_TYPES + (NUM_SUGGESTION_TYPES - 1 - suggestion.getType().ordinal());
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
		Map<CompletionSuggestion, Integer> ratedSuggestions = getRatedSuggestions(text, caretPosition);
		List<Completion> completions = ratedSuggestions.keySet().stream()
			.map(suggestion -> createCompletion(suggestion, ratedSuggestions.get(suggestion)))
			.collect(Collectors.toList());
		Collections.sort(completions,
			Comparator.comparing(Completion::getRelevance)
				.reversed()
				.thenComparing(completion -> completion.toString().toLowerCase()));
		return completions;
	}

	List<ParameterizedCompletion> getParameterizedCompletions(String text, int caretPosition) {
		Map<CompletionSuggestion, Integer> ratedSuggestions = getRatedSuggestions(text, caretPosition);
		return ratedSuggestions.keySet().stream()
			.filter(suggestion -> suggestion.getType() == CompletionSuggestionType.METHOD)
			.map(suggestion -> createParameterizedCompletion((CompletionSuggestionMethod) suggestion, ratedSuggestions.get(suggestion)))
			.collect(Collectors.toList());
	}

	private Map<CompletionSuggestion, Integer> getRatedSuggestions(String text, int caretPosition) {
		Map<CompletionSuggestion, Integer> ratedSuggestions;
		Throwable throwable = null;
		try {
			ratedSuggestions = suggestionProvider.provideRatedSuggestions(text, caretPosition);
		} catch (Throwable t) {
			throwable = t;
			ratedSuggestions = ImmutableMap.of();
		}
		if (exceptionConsumer != null) {
			exceptionConsumer.accept(throwable);
		}
		return ratedSuggestions;
	}

	private Completion createCompletion(CompletionSuggestion suggestion, int rating) {
		if (suggestion.getType() == CompletionSuggestionType.METHOD) {
			return createParameterizedCompletion((CompletionSuggestionMethod) suggestion, rating);
		}
		int relevance = getRelevance(suggestion, rating);
		return new CustomBasicCompletion(suggestion, relevance, completionProvider);
	}

	private ParameterizedCompletion createParameterizedCompletion(CompletionSuggestionMethod suggestion, int rating) {
		int relevance = getRelevance(suggestion, rating);
		return new CustomFunctionCompletion(suggestion, relevance, completionProvider);
	}
}
