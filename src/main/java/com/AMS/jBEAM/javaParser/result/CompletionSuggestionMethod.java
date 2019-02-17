package com.AMS.jBEAM.javaParser.result;

import com.AMS.jBEAM.javaParser.utils.dataProviders.ExecutableDataProvider;
import com.AMS.jBEAM.javaParser.utils.wrappers.ExecutableInfo;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompletionSuggestionMethod implements CompletionSuggestionIF
{
	private final ExecutableInfo	methodInfo;
	private final int	 			insertionBegin;
	private final int 				insertionEnd;

	public CompletionSuggestionMethod(ExecutableInfo methodInfo, int insertionBegin, int insertionEnd) {
		this.methodInfo = methodInfo;
		this.insertionBegin = insertionBegin;
		this.insertionEnd = insertionEnd;
	}

	@Override
	public IntRange getInsertionRange() {
		return new IntRange(insertionBegin, insertionEnd);
	}

	@Override
	public int getCaretPositionAfterInsertion() {
		return insertionBegin + methodInfo.getName().length() + (methodInfo.getNumberOfArguments() == 0 ? 2 : 1);
	}

	@Override
	public String getTextToInsert() {
		return methodInfo.getName()
				+ "("
				+ IntStream.range(0, methodInfo.getNumberOfArguments()).mapToObj(param -> "").collect(Collectors.joining(", "))
				+ ")";
	}

	@Override
	public String toString() {
		return ExecutableDataProvider.getMethodDisplayText(methodInfo);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionMethod that = (CompletionSuggestionMethod) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(methodInfo, that.methodInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(methodInfo, insertionBegin, insertionEnd);
	}
}
