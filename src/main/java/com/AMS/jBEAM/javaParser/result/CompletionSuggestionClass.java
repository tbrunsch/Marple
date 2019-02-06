package com.AMS.jBEAM.javaParser.result;

import com.AMS.jBEAM.javaParser.utils.ClassDataProvider;
import com.AMS.jBEAM.javaParser.utils.ClassInfo;

import java.util.Objects;

public class CompletionSuggestionClass implements CompletionSuggestionIF
{
	private final ClassInfo classInfo;
	private final int 			insertionBegin;
	private final int 			insertionEnd;

	public CompletionSuggestionClass(ClassInfo classInfo, int insertionBegin, int insertionEnd) {
		this.classInfo = classInfo;
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
		return classInfo.getSimpleNameWithoutLeadingDigits();
	}

	@Override
	public String toString() {
		return ClassDataProvider.getClassDisplayText(classInfo);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionClass that = (CompletionSuggestionClass) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(classInfo, that.classInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(classInfo, insertionBegin, insertionEnd);
	}
}
