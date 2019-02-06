package com.AMS.jBEAM.javaParser.result;

import com.AMS.jBEAM.javaParser.utils.ClassDataProvider;

import java.util.Objects;

public class CompletionSuggestionPackage implements CompletionSuggestionIF
{
	private final Package	pack;
	private final int 		insertionBegin;
	private final int 		insertionEnd;

	public CompletionSuggestionPackage(Package pack, int insertionBegin, int insertionEnd) {
		this.pack = pack;
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
		String packageName = pack.getName();
		int lastDotIndex = packageName.lastIndexOf('.');
		return lastDotIndex < 0 ? packageName : packageName.substring(lastDotIndex + 1);
	}

	@Override
	public String toString() {
		return ClassDataProvider.getPackageDisplayText(pack);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionPackage that = (CompletionSuggestionPackage) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(pack, that.pack);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pack, insertionBegin, insertionEnd);
	}
}
