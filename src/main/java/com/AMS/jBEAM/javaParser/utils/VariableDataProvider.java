package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.settings.Variable;
import com.AMS.jBEAM.javaParser.settings.VariablePool;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionIF;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionVariable;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.google.common.reflect.TypeToken;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class VariableDataProvider
{
	private final VariablePool variablePool;

	public VariableDataProvider(VariablePool variablePool) {
		this.variablePool = variablePool;
	}

	public CompletionSuggestions suggestVariables(String expectedName, List<TypeToken<?>> expectedTypes, int insertionBegin, int insertionEnd) {
		List<Variable> variables = variablePool.getVariables().stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			variables,
			variable -> new CompletionSuggestionVariable(variable, insertionBegin, insertionEnd),
			rateVariableByNameAndTypesFunc(expectedName, expectedTypes)
		);
		return new CompletionSuggestions(ratedSuggestions);
	}

	private static int rateVariableByName(Variable variable, String expectedName) {
		return ParseUtils.rateStringMatch(variable.getName(), expectedName);
	}

	private static int rateVariableByTypes(Variable variable, List<TypeToken<?>> expectedTypes) {
		Object value = variable.getValue();
		return	expectedTypes == null	? ParseUtils.TYPE_MATCH_FULL :
				expectedTypes.isEmpty()	? ParseUtils.TYPE_MATCH_NONE
										: expectedTypes.stream().mapToInt(expectedType -> ParseUtils.rateTypeMatch(value == null ? null : TypeToken.of(value.getClass()), expectedType)).min().getAsInt();
	}

	private static ToIntFunction<Variable> rateVariableByNameAndTypesFunc(String variableName, List<TypeToken<?>> expectedTypes) {
		return variable -> (ParseUtils.TYPE_MATCH_NONE + 1)*rateVariableByName(variable, variableName) + rateVariableByTypes(variable, expectedTypes);
	}

	public static String getVariableDisplayText(Variable variable) {
		return variable.getName();
	}

}
