package com.AMS.jBEAM.javaParser;

public interface CompletionSuggestionIF
{
	IntRange getInsertionRange();
	int getCaretPositionAfterInsertion();
	String getTextToInsert();
	String toString();
}
