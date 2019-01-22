package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ExecutableInfo;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;

/**
 * Parses a sub expression starting with a method {@code <method>}, assuming the context
 * <ul>
 *     <li>{@code <context instance>.<method>},</li>
 *     <li>{@code <context class>.<method>}, or</li>
 *     <li>{@code <fmethod} (like {@code <context instance>.<method>} for {@code <context instance> = this})</li>
 * </ul>
 */
public class MethodParser extends AbstractEntityParser
{
	private final boolean staticOnly;

	public MethodParser(ParserContext parserContext, ObjectInfo thisInfo, boolean staticOnly) {
		super(parserContext, thisInfo);
		this.staticOnly = staticOnly;
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
			log(LogLevel.INFO, "suggesting methods for completion...");
			return parserContext.getExecutableDataProvider().suggestMethods(currentContextInfo, expectedResultTypes, startPosition, insertionEnd, staticOnly);
		}

		if (currentContextInfo.getDeclaredType() == null && !staticOnly) {
			log(LogLevel.ERROR, "null pointer exception");
			return new ParseError(startPosition, "Null pointer exception", ErrorType.WRONG_PARSER);
		}

		Token methodNameToken;
		try {
			methodNameToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "missing method name at " + tokenStream);
			return new ParseError(startPosition, "Expected an identifier", ErrorType.WRONG_PARSER);
		}
		String methodName = methodNameToken.getValue();
		final int endPosition = tokenStream.getPosition();

		TypeToken<?> currentContextType = parserContext.getObjectInfoProvider().getType(currentContextInfo);
		List<ExecutableInfo> methodInfos = parserContext.getInspectionDataProvider().getMethodInfos(currentContextType, staticOnly);

		// check for code completion
		if (methodNameToken.isContainsCaret()) {
			log(LogLevel.SUCCESS, "suggesting methods matching '" + methodName + "'");
			Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
				methodInfos,
				methodInfo -> new CompletionSuggestionMethod(methodInfo, startPosition, endPosition),
				ParseUtils.rateMethodByNameAndTypesFunc(methodName, expectedResultTypes)
			);
			return new CompletionSuggestions(ratedSuggestions);
		}

		if (!tokenStream.hasMore() || tokenStream.peekCharacter() != '(') {
			log(LogLevel.ERROR, "missing '(' at " + tokenStream);
			return new ParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		// no code completion requested => method name must exist
		List<ExecutableInfo> matchingMethodInfos = methodInfos.stream().filter(methodInfo -> methodInfo.getName().equals(methodName)).collect(Collectors.toList());
		if (matchingMethodInfos.isEmpty()) {
			log(LogLevel.ERROR, "unknown method '" + methodName + "'");
			return new ParseError(startPosition, "Unknown method '" + methodName + "'", ErrorType.SEMANTIC_ERROR);
		}

		log(LogLevel.INFO, "parsing method arguments");
		List<ParseResultIF> argumentParseResults = parserContext.getExecutableDataProvider().parseExecutableArguments(tokenStream, matchingMethodInfos);

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
		List<ExecutableInfo> bestMatchingMethodInfos = parserContext.getExecutableDataProvider().getBestMatchingExecutableInfos(matchingMethodInfos, argumentInfos);

		switch (bestMatchingMethodInfos.size()) {
			case 0:
				log(LogLevel.ERROR, "no matching method found");
				return new ParseError(tokenStream.getPosition(), "No method matches the given arguments", ErrorType.SEMANTIC_ERROR);
			case 1: {
				ExecutableInfo bestMatchingExecutableInfo = bestMatchingMethodInfos.get(0);
				ObjectInfo methodReturnInfo;
				try {
					methodReturnInfo = parserContext.getObjectInfoProvider().getExecutableReturnInfo(currentContextInfo, bestMatchingExecutableInfo, argumentInfos);
					log(LogLevel.SUCCESS, "found unique matching method");
				} catch (Exception e) {
					log(LogLevel.ERROR, "caught exception: " + e.getMessage());
					return new ParseError(startPosition, "Exception during method evaluation", ErrorType.EVALUATION_EXCEPTION, e);
				}
				return parserContext.getTailParser(false).parse(tokenStream, methodReturnInfo, expectedResultTypes);
			}
			default: {
				String error = "Ambiguous method call. Possible candidates are:\n"
								+ bestMatchingMethodInfos.stream().map(MethodParser::formatMethodInfo).collect(Collectors.joining("\n"));
				log(LogLevel.ERROR, error);
				return new AmbiguousParseResult(tokenStream.getPosition(), error);
			}
		}
	}

	private static String formatMethodInfo(ExecutableInfo methodInfo) {
		return methodInfo.getName()
				+ "("
				+ methodInfo.formatArguments()
				+ ")";
	}
}
