package com.AMS.jBEAM.javaParser.result;

import com.AMS.jBEAM.javaParser.Variable;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;

import java.util.Objects;

public class CompletionSuggestionVariable implements CompletionSuggestionIF
{
	private final Variable	variable;
	private final int 		insertionBegin;
	private final int 		insertionEnd;

	public CompletionSuggestionVariable(Variable variable, int insertionBegin, int insertionEnd) {
		this.variable = variable;
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
		return ParseUtils.getVariableDisplayText(variable);
	}

	@Override
	public String toString() {
		return ParseUtils.getVariableDisplayText(variable);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionVariable that = (CompletionSuggestionVariable) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(variable, that.variable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(variable, insertionBegin, insertionEnd);
	}
}
