package com.AMS.jBEAM.javaParser.result;

import com.AMS.jBEAM.javaParser.utils.ParseUtils;

import java.lang.reflect.Field;
import java.util.Objects;

public class CompletionSuggestionField implements CompletionSuggestionIF
{
	private final Field field;
	private final int 	insertionBegin;
	private final int 	insertionEnd;

	public CompletionSuggestionField(Field field, int insertionBegin, int insertionEnd) {
		this.field = field;
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
		return field.getName();
	}

	@Override
	public String toString() {
		return ParseUtils.getFieldDisplayText(field);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionField that = (CompletionSuggestionField) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(field, that.field);
	}

	@Override
	public int hashCode() {
		return Objects.hash(field, insertionBegin, insertionEnd);
	}
}
