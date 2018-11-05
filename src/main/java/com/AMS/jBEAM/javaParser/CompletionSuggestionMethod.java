package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

class CompletionSuggestionMethod implements CompletionSuggestionIF
{
	private final Method 	method;
	private final int	 	insertionBegin;
	private final int 		insertionEnd;

	CompletionSuggestionMethod(Method method, int insertionBegin, int insertionEnd) {
		this.method = method;
		this.insertionBegin = insertionBegin;
		this.insertionEnd = insertionEnd;
	}

	@Override
	public IntRange getInsertionRange() {
		return new IntRange(insertionBegin, insertionEnd);
	}

	@Override
	public int getCaretPositionAfterInsertion() {
		return insertionBegin + method.getName().length() + (method.getParameterCount() == 0 ? 2 : 1);
	}

	@Override
	public String getTextToInsert() {
		return method.getName()
				+ "("
				+ Arrays.stream(method.getParameters()).map(param -> "").collect(Collectors.joining(", "))
				+ ")";
	}

	@Override
	public String toString() {
		return ParseUtils.getMethodDisplayText(method);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionMethod that = (CompletionSuggestionMethod) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(method, that.method);
	}

	@Override
	public int hashCode() {
		return Objects.hash(method, insertionBegin, insertionEnd);
	}
}
