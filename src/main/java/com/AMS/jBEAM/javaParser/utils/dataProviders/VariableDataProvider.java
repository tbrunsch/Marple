package com.AMS.jBEAM.javaParser.utils.dataProviders;

import com.AMS.jBEAM.javaParser.parsers.ParseExpectation;
import com.AMS.jBEAM.javaParser.settings.Variable;
import com.AMS.jBEAM.javaParser.settings.VariablePool;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionIF;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionVariable;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
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

	public CompletionSuggestions suggestVariables(String expectedName, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		List<Variable> variables = variablePool.getVariables().stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			variables,
			variable -> new CompletionSuggestionVariable(variable, insertionBegin, insertionEnd),
			rateVariableByNameAndTypesFunc(expectedName, expectation)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private int rateVariableByName(Variable variable, String expectedName) {
		return ParseUtils.rateStringMatch(variable.getName(), expectedName);
	}

	private int rateVariableByTypes(Variable variable, ParseExpectation expectation) {
		List<TypeToken<?>> allowedTypes = expectation.getAllowedTypes();
		Object value = variable.getValue();
		return	allowedTypes == null	? ParseUtils.TYPE_MATCH_FULL :
				allowedTypes.isEmpty()	? ParseUtils.TYPE_MATCH_NONE
										: allowedTypes.stream().mapToInt(allowedType -> ParseUtils.rateTypeMatch(value == null ? null : TypeToken.of(value.getClass()), allowedType)).min().getAsInt();
	}

	private ToIntFunction<Variable> rateVariableByNameAndTypesFunc(String variableName, ParseExpectation expectation) {
		return variable -> (ParseUtils.TYPE_MATCH_NONE + 1)*rateVariableByName(variable, variableName) + rateVariableByTypes(variable, expectation);
	}

	public static String getVariableDisplayText(Variable variable) {
		return variable.getName();
	}
}
