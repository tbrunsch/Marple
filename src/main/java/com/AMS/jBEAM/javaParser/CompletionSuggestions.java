package com.AMS.jBEAM.javaParser;

import java.util.Collections;
import java.util.Map;

class CompletionSuggestions implements ParseResultIF
{
	static final CompletionSuggestions NONE	= new CompletionSuggestions(Collections.emptyMap());

    private final Map<CompletionSuggestionIF, Integer> ratedSuggestions;

    CompletionSuggestions(Map<CompletionSuggestionIF, Integer> ratedSuggestions) {
        this.ratedSuggestions = ratedSuggestions;
    }

    @Override
    public ParseResultType getResultType() {
        return ParseResultType.COMPLETION_SUGGESTIONS;
    }

	Map<CompletionSuggestionIF, Integer> getRatedSuggestions() {
        return ratedSuggestions;
    }
}
