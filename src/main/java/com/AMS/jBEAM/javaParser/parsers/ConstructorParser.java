package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ExecutableInfo;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Modifier;
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
			log(LogLevel.ERROR, "'new' operator expected");
			return new ParseError(startPosition, "Expected operator 'new'", ErrorType.WRONG_PARSER);
		}
		if (operatorToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available");
			return CompletionSuggestions.NONE;
		}
		if (!operatorToken.getValue().equals("new")) {
			log(LogLevel.ERROR, "'new' operator expected");
			return new ParseError(startPosition, "Expected operator 'new'", ErrorType.WRONG_PARSER);
		}

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ParseResultIF classParseResult = parserContext.getClassDataProvider().readClass(tokenStream, expectedResultTypes, false, false);
		log(LogLevel.INFO, "parse result: " + classParseResult.getResultType());

		// propagate anything except results
		if (classParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return classParseResult;
		}

		ParseResult parseResult = (ParseResult) classParseResult;
		int parsedToPosition = parseResult.getParsedToPosition();
		ObjectInfo constructorObjectInfo = parseResult.getObjectInfo();

		tokenStream.moveTo(parsedToPosition);

		if (!tokenStream.hasMore() || tokenStream.peekCharacter() != '(') {
			log(LogLevel.ERROR, "missing '(' at " + tokenStream);
			return new ParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		TypeToken<?> constructorType = constructorObjectInfo.getDeclaredType();
		Class<?> constructorClass = constructorType.getClass();
		if (constructorClass.getEnclosingClass() != null && (constructorClass.getModifiers() & Modifier.STATIC) == 0) {
			log(LogLevel.ERROR, "cannot instantiate non-static inner class");
			return new ParseError(parsedToPosition, "Cannot instantiate inner class '" + constructorClass.getName() + "'", ErrorType.SEMANTIC_ERROR);
		}
		List<ExecutableInfo> constructorInfos = parserContext.getInspectionDataProvider().getConstructorInfos(constructorType);

		log(LogLevel.INFO, "parsing constructor arguments");
		List<ParseResultIF> argumentParseResults = parserContext.getExecutableDataProvider().parseExecutableArguments(tokenStream, constructorInfos);

		if (argumentParseResults.isEmpty()) {
			log(LogLevel.INFO, "no arguments found");
		} else {
			ParseResultIF lastArgumentParseResult = argumentParseResults.get(argumentParseResults.size()-1);
			log(LogLevel.INFO, "parse result: " + lastArgumentParseResult.getResultType());
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
				log(LogLevel.ERROR, "no matching constructor found");
				return new ParseError(tokenStream.getPosition(), "No constructor matches the given arguments", ErrorType.SEMANTIC_ERROR);
			case 1: {
				ExecutableInfo bestMatchingConstructorInfo = bestMatchingConstructorInfos.get(0);
				ObjectInfo constructorReturnInfo;
				try {
					constructorReturnInfo = parserContext.getObjectInfoProvider().getExecutableReturnInfo(constructorObjectInfo, bestMatchingConstructorInfo, argumentInfos);
					log(LogLevel.SUCCESS, "found unique matching constructor");
				} catch (Exception e) {
					log(LogLevel.ERROR, "caught exception: " + e.getMessage());
					return new ParseError(startPosition, "Exception during constructor evaluation", ErrorType.EVALUATION_EXCEPTION, e);
				}
				return parserContext.getTailParser(false).parse(tokenStream, constructorReturnInfo, expectedResultTypes);
			}
			default: {
				String error = "Ambiguous constructor call. Possible candidates are:\n"
								+ bestMatchingConstructorInfos.stream().map(ConstructorParser::formatConstructorInfo).collect(Collectors.joining("\n"));
				log(LogLevel.ERROR, error);
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
