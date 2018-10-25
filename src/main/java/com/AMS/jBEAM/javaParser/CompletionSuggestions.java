package com.AMS.jBEAM.javaParser;

import java.util.List;

class CompletionSuggestions implements ParseResultIF
{
    private final List<CompletionSuggestion> suggestions;

    CompletionSuggestions(List<CompletionSuggestion> suggestions) {
        this.suggestions = suggestions;
    }

    @Override
    public ParseResultType getResultType() {
        return ParseResultType.COMPLETION_SUGGESTIONS;
    }

    List<CompletionSuggestion> getSuggestions() {
        return suggestions;
    }
}
