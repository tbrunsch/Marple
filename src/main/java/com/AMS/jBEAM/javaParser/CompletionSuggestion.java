package com.AMS.jBEAM.javaParser;

class CompletionSuggestion
{
    private final TextInsertionInfo insertionInfo;
    private final String            displayText;
    private final int               rating;

    CompletionSuggestion(TextInsertionInfo insertionInfo, String displayText, int rating) {
        this.insertionInfo = insertionInfo;
        this.displayText = displayText;
        this.rating = rating;
    }

    public TextInsertionInfo getInsertionInfo() {
        return insertionInfo;
    }

    public String getDisplayText() {
        return displayText;
    }

    public int getRating() {
        return rating;
    }
}
