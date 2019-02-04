package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.settings.Variable;
import com.AMS.jBEAM.javaParser.settings.VariablePool;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.google.common.reflect.TypeToken;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.AMS.jBEAM.javaParser.result.ParseError.*;

public class VariableParser extends AbstractEntityParser
{
	public VariableParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes) {
		int startPosition = tokenStream.getPosition();

		if (tokenStream.isCaretAtPosition()) {
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				insertionEnd = startPosition;
			}
			log(LogLevel.INFO, "suggesting variables for completion...");
			return parserContext.getVariableDataProvider().suggestVariables(expectedResultTypes, startPosition, insertionEnd);
		}

		Token variableToken;
		try {
			variableToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "missing variable name at " + tokenStream);
			return new ParseError(startPosition, "Expected a variable name", ErrorType.WRONG_PARSER);
		}
		String variableName = variableToken.getValue();
		int endPosition = tokenStream.getPosition();

		VariablePool variablePool = parserContext.getSettings().getVariablePool();
		List<Variable> variables = variablePool.getVariables().stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());

		// check for code completion
		if (variableToken.isContainsCaret()) {
			log(LogLevel.SUCCESS, "suggesting variables matching '" + variableName + "'");
			Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
				variables,
				variable -> new CompletionSuggestionVariable(variable, startPosition, endPosition),
				ParseUtils.rateVariableByNameAndTypesFunc(variableName, expectedResultTypes)
			);
			return new CompletionSuggestions(ratedSuggestions);
		}

		if (tokenStream.hasMore() && tokenStream.peekCharacter() == '(') {
			log(LogLevel.ERROR, "unexpected '(' at " + tokenStream);
			return new ParseError(tokenStream.getPosition() + 1, "Unexpected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		// no code completion requested => variable name must exist
		Optional<Variable> firstVariableMatch = variables.stream().filter(variable -> variable.getName().equals(variableName)).findFirst();
		if (!firstVariableMatch.isPresent()) {
			log(LogLevel.ERROR, "unknown variable '" + variableName + "'");
			return new ParseError(startPosition, "Unknown variable '" + variableName + "'", ErrorType.SEMANTIC_ERROR);
		}
		log(LogLevel.SUCCESS, "detected variable '" + variableName + "'");

		Variable matchingVariable = firstVariableMatch.get();
		ObjectInfo matchingVariableInfo = parserContext.getObjectInfoProvider().getVariableInfo(matchingVariable);

		return parserContext.getTailParser(false).parse(tokenStream, matchingVariableInfo, expectedResultTypes);
	}
}
