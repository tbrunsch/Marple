package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ExecutableInfo;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConstructorParser extends AbstractEntityParser
{
	public ConstructorParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes) {
		int startPosition = tokenStream.getPosition();
		Token operatorToken = tokenStream.readKeyWordUnchecked();
		if (operatorToken == null) {
			return new ParseError(startPosition, "Expected operator 'new'", ErrorType.WRONG_PARSER);
		}
		if (operatorToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}
		if (!operatorToken.getValue().equals("new")) {
			return new ParseError(startPosition, "Expected operator 'new'", ErrorType.WRONG_PARSER);
		}

		ParseResultIF classParseResult = parserContext.getClassDataProvider().readClass(tokenStream, expectedResultTypes, false, false);

		// propagate anything except results
		if (classParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return classParseResult;
		}

		ParseResult parseResult = (ParseResult) classParseResult;
		int parsedToPosition = parseResult.getParsedToPosition();
		ObjectInfo constructorObjectInfo = parseResult.getObjectInfo();

		tokenStream.moveTo(parsedToPosition);

		if (!tokenStream.hasMore() || tokenStream.peekCharacter() != '(') {
			return new ParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		TypeToken<?> constructorType = constructorObjectInfo.getDeclaredType();
		Class<?> constructorClass = constructorType.getClass();
		if (constructorClass.getEnclosingClass() != null && (constructorClass.getModifiers() & Modifier.STATIC) == 0) {
			return new ParseError(parsedToPosition, "Cannot instantiate inner class '" + constructorClass.getName() + "'", ErrorType.SEMANTIC_ERROR);
		}
		List<ExecutableInfo> constructorInfos = parserContext.getInspectionDataProvider().getConstructorInfos(constructorType);

		List<ParseResultIF> argumentParseResults = parserContext.getExecutableDataProvider().parseExecutableArguments(tokenStream, constructorInfos);

		if (!argumentParseResults.isEmpty()) {
			ParseResultIF lastArgumentParseResult = argumentParseResults.get(argumentParseResults.size()-1);
			if (lastArgumentParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
				// Immediately propagate anything but parse results (code completion, errors, ambiguous parse results)
				return lastArgumentParseResult;
			}
		}

		List<ObjectInfo> argumentInfos = argumentParseResults.stream()
			.map(ParseResult.class::cast)
			.map(ParseResult::getObjectInfo)
			.collect(Collectors.toList());
		List<ExecutableInfo> bestMatchingConstructorInfos = parserContext.getExecutableDataProvider().getBestMatchingExecutableInfos(constructorInfos, argumentInfos);

		switch (bestMatchingConstructorInfos.size()) {
			case 0:
				return new ParseError(tokenStream.getPosition(), "No constructor matches the given arguments", ErrorType.SEMANTIC_ERROR);
			case 1: {
				ExecutableInfo bestMatchingConstructorInfo = bestMatchingConstructorInfos.get(0);
				ObjectInfo constructorReturnInfo;
				try {
					constructorReturnInfo = parserContext.getObjectInfoProvider().getExecutableReturnInfo(constructorObjectInfo, bestMatchingConstructorInfo, argumentInfos);
				} catch (Exception e) {
					return new ParseError(startPosition, "Exception during constructor evaluation", ErrorType.EVALUATION_EXCEPTION, e);
				}
				return parserContext.getTailParser(false).parse(tokenStream, constructorReturnInfo, expectedResultTypes);
			}
			default: {
				String error = "Ambiguous constructor call. Possible candidates are:\n"
								+ bestMatchingConstructorInfos.stream().map(ConstructorParser::formatConstructorInfo).collect(Collectors.joining("\n"));
				return new AmbiguousParseResult(tokenStream.getPosition(), error);
			}
		}
	}

	private static String formatConstructorInfo(ExecutableInfo constructorInfo) {
		return constructorInfo.getName()
				+ "("
				+ constructorInfo.formatArguments()
				+ ")";
	}
}
