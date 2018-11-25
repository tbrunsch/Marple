package com.AMS.jBEAM.javaParser.result;

public interface CompletionSuggestionIF
{
	IntRange getInsertionRange();
	int getCaretPositionAfterInsertion();
	String getTextToInsert();
	String toString();
}
