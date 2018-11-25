package com.AMS.jBEAM.javaParser.result;

import java.util.Collections;
import java.util.Map;

public class CompletionSuggestions implements ParseResultIF
{
	public static final CompletionSuggestions NONE	= new CompletionSuggestions(Collections.emptyMap());

	private final Map<CompletionSuggestionIF, Integer> ratedSuggestions;

	public CompletionSuggestions(Map<CompletionSuggestionIF, Integer> ratedSuggestions) {
		this.ratedSuggestions = ratedSuggestions;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.COMPLETION_SUGGESTIONS;
	}

	public Map<CompletionSuggestionIF, Integer> getRatedSuggestions() {
		return ratedSuggestions;
	}
}
