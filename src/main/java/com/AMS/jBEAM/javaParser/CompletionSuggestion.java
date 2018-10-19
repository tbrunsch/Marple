package com.AMS.jBEAM.javaParser;

class CompletionSuggestion implements CompletionSuggestionIF
{
    private final String    suggestion;
    private final int       rating;

    CompletionSuggestion(String suggestion, int rating) {
        this.suggestion = suggestion;
        this.rating = rating;
    }

    @Override
    public String getSuggestion() {
        return suggestion;
    }

    @Override
    public int getRating() {
        return rating;
    }
}
