package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.Variable;
import com.AMS.jBEAM.javaParser.VariablePoolIF;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionIF;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionVariable;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariableDataProvider
{
	private final VariablePoolIF	variablePool;

	public VariableDataProvider(VariablePoolIF variablePool) {
		this.variablePool = variablePool;
	}

	public CompletionSuggestions suggestVariables(List<Class<?>> expectedClasses, final int insertionBegin, final int insertionEnd) {
		List<Variable> variables = variablePool.getVariables().stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			variables,
			variable -> new CompletionSuggestionVariable(variable, insertionBegin, insertionEnd),
			ParseUtils.rateVariableByClassesFunc(expectedClasses)
		);
		return new CompletionSuggestions(ratedSuggestions);
	}
}
