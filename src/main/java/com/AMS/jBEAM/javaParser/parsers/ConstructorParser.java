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
import java.util.ArrayList;
import java.util.Collections;
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
		ParseResultIF classParseResult = parserContext.getClassDataProvider().readClass(tokenStream, false);
		log(LogLevel.INFO, "parse result: " + classParseResult.getResultType());

		// propagate anything except results
		if (classParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return classParseResult;
		}

		ParseResult parseResult = (ParseResult) classParseResult;
		int parsedToPosition = parseResult.getParsedToPosition();
		ObjectInfo classInfo = parseResult.getObjectInfo();

		tokenStream.moveTo(parsedToPosition);

		char nextChar = tokenStream.peekCharacter();
		if (nextChar == '(') {
			return parseObjectConstructor(tokenStream, startPosition, classInfo, expectedResultTypes);
		} else if (nextChar == '[') {
			return parseArrayConstructor(tokenStream, startPosition, classInfo, expectedResultTypes);
		} else {
			log(LogLevel.ERROR, "missing '(' at " + tokenStream);
			return new ParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}
	}

	private ParseResultIF parseObjectConstructor(TokenStream tokenStream, int startPosition, ObjectInfo classInfo, List<TypeToken<?>> expectedResultTypes) {
		TypeToken<?> constructorType = classInfo.getDeclaredType();
		Class<?> constructorClass = constructorType.getClass();
		if (constructorClass.getEnclosingClass() != null && !Modifier.isStatic(constructorClass.getModifiers())) {
			log(LogLevel.ERROR, "cannot instantiate non-static inner class");
			return new ParseError(tokenStream.getPosition(), "Cannot instantiate inner class '" + constructorClass.getName() + "'", ErrorType.SEMANTIC_ERROR);
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
					constructorReturnInfo = parserContext.getObjectInfoProvider().getExecutableReturnInfo(classInfo, bestMatchingConstructorInfo, argumentInfos);
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

	private ParseResultIF parseArrayConstructor(TokenStream tokenStream, int startPosition, ObjectInfo classInfo, List<TypeToken<?>> expectedResultTypes) {
		// TODO: currently, only 1d arrays are supported
		ParseResultIF arraySizeParseResult = parseArraySize(tokenStream);

		if (arraySizeParseResult == null) {
			// array constructor with initializer list (e.g., "new int[] { 1, 2, 3 }")
			log(LogLevel.INFO, "parsing array elements at " + tokenStream);
			List<ParseResultIF> elementParseResults = parseArrayElements(tokenStream, Collections.singletonList(classInfo.getDeclaredType()));

			if (elementParseResults.isEmpty()) {
				log(LogLevel.INFO, "detected empty array");
			} else {
				ParseResultIF lastArgumentParseResult = elementParseResults.get(elementParseResults.size()-1);
				log(LogLevel.INFO, "parse result: " + lastArgumentParseResult.getResultType());
				if (lastArgumentParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
					// propagate anything but parse results (code completion, errors, ambiguous parse results)
					return lastArgumentParseResult;
				}
			}

			List<ObjectInfo> elementInfos = elementParseResults.stream().map(ParseResult.class::cast).map(ParseResult::getObjectInfo).collect(Collectors.toList());
			ObjectInfo arrayInfo = parserContext.getObjectInfoProvider().getArrayInfo(classInfo, elementInfos);
			log (LogLevel.SUCCESS, "detected valid array construction with initializer list");
			return parserContext.getTailParser(false).parse(tokenStream, arrayInfo, expectedResultTypes);
		} else {
			// array constructor with default initialization (e.g., "new int[3]")

			// propagate anything except results
			if (arraySizeParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
				return arraySizeParseResult;
			}

			ParseResult parseResult = (ParseResult) arraySizeParseResult;
			int parsedToPosition = parseResult.getParsedToPosition();
			ObjectInfo sizeInfo = parseResult.getObjectInfo();
			ObjectInfo arrayInfo;
			try {
				arrayInfo = parserContext.getObjectInfoProvider().getArrayInfo(classInfo, sizeInfo);
				log(LogLevel.SUCCESS, "detected valid array construction with null initialization");
			} catch (ClassCastException | NegativeArraySizeException e) {
				log(LogLevel.ERROR, "caught exception: " + e.getMessage());
				return new ParseError(startPosition, e.getClass().getSimpleName() + " during array construction", ErrorType.EVALUATION_EXCEPTION, e);
			}
			tokenStream.moveTo(parsedToPosition);
			return parserContext.getTailParser(false).parse(tokenStream, arrayInfo, expectedResultTypes);
		}
	}

	// returns null if no size is specified (e.g. in "new int[] { 1, 2, 3 }")
	private ParseResultIF parseArraySize(TokenStream tokenStream) {
		log(LogLevel.INFO, "parsing array size");

		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");

		if (tokenStream.peekCharacter() == ']') {
			tokenStream.readCharacterUnchecked();
			return null;
		}

		List<TypeToken<?>> expectedResultTypes = Collections.singletonList(TypeToken.of(int.class));
		ParseResultIF arraySizeParseResult = parserContext.getCompoundExpressionParser().parse(tokenStream, thisInfo, expectedResultTypes);

		// propagate anything except results
		if (arraySizeParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return arraySizeParseResult;
		}

		ParseResult parseResult = ((ParseResult) arraySizeParseResult);
		int parsedToPosition = parseResult.getParsedToPosition();

		tokenStream.moveTo(parsedToPosition);
		characterToken = tokenStream.readCharacterUnchecked();

		if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
			log(LogLevel.ERROR, "missing ']' at " + tokenStream);
			return new ParseError(parsedToPosition, "Expected closing bracket ']'", ErrorType.SYNTAX_ERROR);
		}

		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.NONE;
		}

		// propagate parse result with corrected position (includes ']')
		return new ParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
	}

	private List<ParseResultIF> parseArrayElements(TokenStream tokenStream, List<TypeToken<?>> expectedResultTypes) {
		List<ParseResultIF> elements = new ArrayList<>();

		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '{') {
			log(LogLevel.ERROR, "missing '{'");
			elements.add(new ParseError(position, "Expected opening curly bracket '{'", ErrorType.SYNTAX_ERROR));
			return elements;
		}

		if (!characterToken.isContainsCaret()) {
			if (!tokenStream.hasMore()) {
				elements.add(new ParseError(tokenStream.getPosition(), "Expected element or closing curly bracket '}'", ParseError.ErrorType.SYNTAX_ERROR));
				return elements;
			}

			char nextCharacter = tokenStream.peekCharacter();
			if (nextCharacter == '}') {
				tokenStream.readCharacterUnchecked();
				return elements;
			}
		}

		while (true) {
			/*
			 * Parse expression for argument i
			 */
			ParseResultIF element = parserContext.getCompoundExpressionParser().parse(tokenStream, parserContext.getThisInfo(), expectedResultTypes);
			elements.add(element);

			// always stop except for results
			if (element.getResultType() != ParseResultType.PARSE_RESULT) {
				return elements;
			}

			ParseResult parseResult = ((ParseResult) element);
			int parsedToPosition = parseResult.getParsedToPosition();
			tokenStream.moveTo(parsedToPosition);

			position = tokenStream.getPosition();
			characterToken = tokenStream.readCharacterUnchecked();

			if (characterToken == null) {
				elements.add(new ParseError(position, "Expected comma ',' or closing curly bracket '}'", ParseError.ErrorType.SYNTAX_ERROR));
				return elements;
			}

			if (characterToken.getValue().charAt(0) == '}') {
				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after '}'
					elements.add(CompletionSuggestions.NONE);
				}
				return elements;
			}

			if (characterToken.getValue().charAt(0) != ',') {
				elements.add(new ParseError(position, "Expected comma ',' or closing curly bracket '}'", ParseError.ErrorType.SYNTAX_ERROR));
				return elements;
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
