package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ExecutableInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;

import java.util.List;
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
abstract class AbstractMethodParser<C> extends AbstractEntityParser<C>
{
	public AbstractMethodParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	abstract boolean contextCausesNullPointerException(C context);
	abstract Object getContextObject(C context);
	abstract List<ExecutableInfo> getMethodInfos(C context);

	@Override
	ParseResultIF doParse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();

		if (contextCausesNullPointerException(context)) {
			log(LogLevel.ERROR, "null pointer exception");
			return new ParseError(startPosition, "Null pointer exception", ErrorType.WRONG_PARSER);
		}

		if (tokenStream.isCaretAtPosition()) {
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				insertionEnd = startPosition;
			}
			log(LogLevel.INFO, "suggesting methods for completion...");
			return suggestMethods("", context, expectation, startPosition, insertionEnd);
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

		// check for code completion
		if (methodNameToken.isContainsCaret()) {
			log(LogLevel.SUCCESS, "suggesting methods matching '" + methodName + "'");
			return suggestMethods(methodName, context, expectation, startPosition, endPosition);
		}

		if (!tokenStream.hasMore() || tokenStream.peekCharacter() != '(') {
			log(LogLevel.ERROR, "missing '(' at " + tokenStream);
			return new ParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		// no code completion requested => method name must exist
		List<ExecutableInfo> methodInfos = getMethodInfos(context);
		List<ExecutableInfo> matchingMethodInfos = methodInfos.stream().filter(methodInfo -> methodInfo.getName().equals(methodName)).collect(Collectors.toList());
		if (matchingMethodInfos.isEmpty()) {
			log(LogLevel.ERROR, "unknown method '" + methodName + "'");
			return new ParseError(startPosition, "Unknown method '" + methodName + "'", ErrorType.SEMANTIC_ERROR);
		}
		log(LogLevel.SUCCESS, "detected " + matchingMethodInfos.size() + " method(s) '" + methodName + "'");

		log(LogLevel.INFO, "parsing method arguments");
		List<ParseResultIF> argumentParseResults = parserToolbox.getExecutableDataProvider().parseExecutableArguments(tokenStream, matchingMethodInfos);

		if (argumentParseResults.isEmpty()) {
			log(LogLevel.INFO, "no arguments found");
		} else {
			ParseResultIF lastArgumentParseResult = argumentParseResults.get(argumentParseResults.size()-1);
			ParseResultType lastArgumentParseResultType = lastArgumentParseResult.getResultType();
			log(LogLevel.INFO, "parse result: " + lastArgumentParseResultType);

			if (ParseUtils.propagateParseResult(lastArgumentParseResult, ParseExpectation.OBJECT)) {
				return lastArgumentParseResult;
			}
		}

		List<ObjectInfo> argumentInfos = argumentParseResults.stream()
			.map(ObjectParseResult.class::cast)
			.map(ObjectParseResult::getObjectInfo)
			.collect(Collectors.toList());
		List<ExecutableInfo> bestMatchingMethodInfos = parserToolbox.getExecutableDataProvider().getBestMatchingExecutableInfos(matchingMethodInfos, argumentInfos);

		switch (bestMatchingMethodInfos.size()) {
			case 0:
				log(LogLevel.ERROR, "no matching method found");
				return new ParseError(tokenStream.getPosition(), "No method matches the given arguments", ErrorType.SEMANTIC_ERROR);
			case 1: {
				ExecutableInfo bestMatchingExecutableInfo = bestMatchingMethodInfos.get(0);
				ObjectInfo methodReturnInfo;
				try {
					methodReturnInfo = parserToolbox.getObjectInfoProvider().getExecutableReturnInfo(getContextObject(context), bestMatchingExecutableInfo, argumentInfos);
					log(LogLevel.SUCCESS, "found unique matching method");
				} catch (Exception e) {
					log(LogLevel.ERROR, "caught exception: " + e.getMessage());
					return new ParseError(startPosition, "Exception during method evaluation", ErrorType.EVALUATION_EXCEPTION, e);
				}
				return parserToolbox.getObjectTailParser().parse(tokenStream, methodReturnInfo, expectation);
			}
			default: {
				String error = "Ambiguous method call. Possible candidates are:\n"
								+ bestMatchingMethodInfos.stream().map(Object::toString).collect(Collectors.joining("\n"));
				log(LogLevel.ERROR, error);
				return new AmbiguousParseResult(tokenStream.getPosition(), error);
			}
		}
	}

	private CompletionSuggestions suggestMethods(String expectedName, C context, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		return parserToolbox.getExecutableDataProvider().suggestMethods(expectedName, getMethodInfos(context), expectation, insertionBegin, insertionEnd);
	}
}
