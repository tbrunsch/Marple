package com.AMS.jBEAM.javaParser.result;

import com.AMS.jBEAM.javaParser.utils.dataProviders.ClassDataProvider;

import java.util.Objects;

public class CompletionSuggestionPackage implements CompletionSuggestionIF
{
	private final String	packageName;
	private final int 		insertionBegin;
	private final int 		insertionEnd;

	public CompletionSuggestionPackage(String packageName, int insertionBegin, int insertionEnd) {
		this.packageName = packageName;
		this.insertionBegin = insertionBegin;
		this.insertionEnd = insertionEnd;
	}

	@Override
	public IntRange getInsertionRange() {
		return new IntRange(insertionBegin, insertionEnd);
	}

	@Override
	public int getCaretPositionAfterInsertion() {
		return insertionBegin + getTextToInsert().length();
	}

	@Override
	public String getTextToInsert() {
		int lastDotIndex = packageName.lastIndexOf('.');
		return lastDotIndex < 0 ? packageName : packageName.substring(lastDotIndex + 1);
	}

	@Override
	public String toString() {
		return packageName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionPackage that = (CompletionSuggestionPackage) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(packageName, that.packageName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(packageName, insertionBegin, insertionEnd);
	}
}
