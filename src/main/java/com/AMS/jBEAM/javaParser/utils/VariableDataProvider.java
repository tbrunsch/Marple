package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.Variable;
import com.AMS.jBEAM.javaParser.VariablePool;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionIF;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionVariable;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.google.common.reflect.TypeToken;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariableDataProvider
{
	private final VariablePool variablePool;

	public VariableDataProvider(VariablePool variablePool) {
		this.variablePool = variablePool;
	}

	public CompletionSuggestions suggestVariables(List<TypeToken<?>> expectedTypes, int insertionBegin, int insertionEnd) {
		List<Variable> variables = variablePool.getVariables().stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			variables,
			variable -> new CompletionSuggestionVariable(variable, insertionBegin, insertionEnd),
			ParseUtils.rateVariableByTypesFunc(expectedTypes)
		);
		return new CompletionSuggestions(ratedSuggestions);
	}
}
