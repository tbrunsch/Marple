package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.google.common.reflect.TypeToken;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExecutableDataProvider
{
	private final ParserContext parserContext;

	public ExecutableDataProvider(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	public CompletionSuggestions suggestMethods(ObjectInfo contextInfo, List<TypeToken<?>> expectedTypes, final int insertionBegin, final int insertionEnd, boolean staticOnly) {
		TypeToken<?> contextType = parserContext.getObjectInfoProvider().getType(contextInfo);
		List<ExecutableInfo> methodInfos = parserContext.getInspectionDataProvider().getMethodInfos(contextType, staticOnly);
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			methodInfos,
			methodInfo -> new CompletionSuggestionMethod(methodInfo, insertionBegin, insertionEnd),
			ParseUtils.rateMethodByTypesFunc(expectedTypes)
		);

		return new CompletionSuggestions(ratedSuggestions);
	}

	public List<ParseResultIF> parseExecutableArguments(TokenStream tokenStream, List<ExecutableInfo> availableExecutableInfos) {
		List<ParseResultIF> arguments = new ArrayList<>();

		int position;
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken != null && characterToken.getValue().charAt(0) == '(';

		if (!characterToken.isContainsCaret()) {
			if (!tokenStream.hasMore()) {
				arguments.add(new ParseError(tokenStream.getPosition(), "Expected argument or closing parenthesis ')'", ParseError.ErrorType.SYNTAX_ERROR));
				return arguments;
			}

			char nextCharacter = tokenStream.peekCharacter();
			if (nextCharacter == ')') {
				tokenStream.readCharacterUnchecked();
				return arguments;
			}
		}

		for (int argIndex = 0; ; argIndex++) {
			final int i = argIndex;

			availableExecutableInfos = availableExecutableInfos.stream().filter(executableInfo -> executableInfo.isArgumentIndexValid(i)).collect(Collectors.toList());
			List<TypeToken<?>> expectedArgumentTypes_i = getExpectedArgumentTypes(availableExecutableInfos, i);

			if (expectedArgumentTypes_i.isEmpty()) {
				arguments.add(new ParseError(tokenStream.getPosition(), "No further arguments expected", ParseError.ErrorType.SEMANTIC_ERROR));
				return arguments;
			}

			/*
			 * Parse expression for argument i
			 */
			ParseResultIF argumentParseResult_i = parserContext.getCompoundExpressionParser().parse(tokenStream, parserContext.getThisInfo(), expectedArgumentTypes_i);
			arguments.add(argumentParseResult_i);

			// always stop except for results
			if (argumentParseResult_i.getResultType() != ParseResultType.PARSE_RESULT) {
				return arguments;
			}

			ParseResult parseResult = ((ParseResult) argumentParseResult_i);
			int parsedToPosition = parseResult.getParsedToPosition();
			tokenStream.moveTo(parsedToPosition);
			ObjectInfo argumentInfo = parseResult.getObjectInfo();
			availableExecutableInfos = availableExecutableInfos.stream().filter(executableInfo -> acceptsArgumentInfo(executableInfo, i, argumentInfo)).collect(Collectors.toList());

			position = tokenStream.getPosition();
			characterToken = tokenStream.readCharacterUnchecked();

			if (characterToken == null) {
				arguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'", ParseError.ErrorType.SYNTAX_ERROR));
				return arguments;
			}

			if (characterToken.getValue().charAt(0) == ')') {
				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after ')'
					arguments.add(CompletionSuggestions.NONE);
				}
				return arguments;
			}

			if (characterToken.getValue().charAt(0) != ',') {
				arguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'", ParseError.ErrorType.SYNTAX_ERROR));
				return arguments;
			}
		}
	}

	// assumes that each of the executableInfos accepts an argument for index argIndex
	private List<TypeToken<?>> getExpectedArgumentTypes(List<ExecutableInfo> executableInfos, int argIndex) {
		return executableInfos.stream()
				.map(executableInfo -> executableInfo.getExpectedArgumentType(argIndex))
				.distinct()
				.collect(Collectors.toList());
	}

	private boolean acceptsArgumentInfo(ExecutableInfo executableInfo, int argIndex, ObjectInfo argInfo) {
		TypeToken<?> expectedArgumentType = executableInfo.getExpectedArgumentType(argIndex);
		TypeToken<?> argumentType = parserContext.getObjectInfoProvider().getType(argInfo);
		return ParseUtils.isConvertibleTo(argumentType, expectedArgumentType);
	}

	public List<ExecutableInfo> getBestMatchingExecutableInfos(List<ExecutableInfo> availableExecutableInfos, List<ObjectInfo> argumentInfos) {
		ObjectInfoProvider objectInfoProvider = parserContext.getObjectInfoProvider();
		List<TypeToken<?>> argumentTypes = argumentInfos.stream().map(objectInfoProvider::getType).collect(Collectors.toList());
		int[] ratings = availableExecutableInfos.stream()
				.mapToInt(executableInfo -> executableInfo.rateArgumentMatch(argumentTypes))
				.toArray();

		List<ExecutableInfo> executableInfos;

		int[][] allowedRatingsByPhase = {
				{ ParseUtils.TYPE_MATCH_FULL},
				{ ParseUtils.TYPE_MATCH_INHERITANCE,	ParseUtils.TYPE_MATCH_PRIMITIVE_CONVERSION},
				{ ParseUtils.TYPE_MATCH_BOXED,			ParseUtils.TYPE_MATCH_BOXED_AND_CONVERSION,	ParseUtils.TYPE_MATCH_BOXED_AND_INHERITANCE}
		};

		for (boolean allowVariadicExecutables : Arrays.asList(false, true)) {
			for (int[] allowedRatings : allowedRatingsByPhase) {
				executableInfos = filterExecutableInfos(availableExecutableInfos, allowedRatings, allowVariadicExecutables, ratings);
				if (!executableInfos.isEmpty()) {
					return executableInfos;
				}
			}
		}
		return Collections.emptyList();
	}

	private static List<ExecutableInfo> filterExecutableInfos(List<ExecutableInfo> executableInfos, int[] allowedRatings, boolean allowVariadicExecutables, int[] ratings) {
		List<ExecutableInfo> filteredExecutableInfos = new ArrayList<>();
		for (int i = 0; i < executableInfos.size(); i++) {
			int rating = ratings[i];
			if (IntStream.of(allowedRatings).noneMatch(allowedRating -> rating == allowedRating)) {
				continue;
			}
			ExecutableInfo executableInfo = executableInfos.get(i);
			if (allowVariadicExecutables || !executableInfo.isVariadic()) {
				filteredExecutableInfos.add(executableInfo);
			}
		}
		return filteredExecutableInfos;
	}
}
