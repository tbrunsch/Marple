package dd.kms.marple.impl.gui.evaluator.completion;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionClass;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionMethod;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class CompletionsFactory
{
	private static final int NUM_COMPLETION_TYPES = CodeCompletionType.values().length;

	static final Comparator<Completion>	COMPLETION_COMPARATOR	= Comparator.comparing(Completion::getRelevance)
		.reversed()
		.thenComparing(CompletionsFactory::isQualifiedClassCompletion, Boolean::compare)
		.thenComparing(completion -> completion.toString().toLowerCase());

	static int getRelevance(CodeCompletion completion) {
		int rating = completion.getRating().getRatingValue();
		return rating * NUM_COMPLETION_TYPES + (NUM_COMPLETION_TYPES - 1 - completion.getType().ordinal());
	}

	private final CodeCompletionProvider	completionProvider;

	CompletionsFactory(CodeCompletionProvider completionProvider) {
		this.completionProvider = completionProvider;
	}

	List<Completion> getCompletions(String text, int caretPosition) {
		List<CodeCompletion> codeCompletions = getCodeCompletions(text, caretPosition);
		return codeCompletions.stream()
			.map(this::createCompletion)
			.sorted(COMPLETION_COMPARATOR)
			.collect(Collectors.toList());
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
		ParserMediator parserMediator = completionProvider.getParserMediator();
		List<CodeCompletion> completions;
		Throwable throwable = null;
		try {
			completions = parserMediator.provideCodeCompletions(text, caretPosition);
		} catch (Throwable t) {
			throwable = t;
			completions = ImmutableList.of();
		}
		parserMediator.consumeException(throwable);
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

	static boolean isQualifiedClassCompletion(Completion completion) {
		if (!(completion instanceof CustomBasicCompletion)) {
			return false;
		}
		CodeCompletion codeCompletion = ((CustomBasicCompletion) completion).getCodeCompletion();
		return codeCompletion.getType() == CodeCompletionType.CLASS
			&& ((CodeCompletionClass) codeCompletion).isQualifiedCompletion();
	}
}
