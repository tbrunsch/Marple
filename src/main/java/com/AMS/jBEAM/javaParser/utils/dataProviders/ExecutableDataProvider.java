package com.AMS.jBEAM.javaParser.utils.dataProviders;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.parsers.ParseExpectation;
import com.AMS.jBEAM.javaParser.parsers.ParseExpectationBuilder;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.wrappers.ExecutableInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.google.common.reflect.TypeToken;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExecutableDataProvider
{
	private final ParserToolbox parserToolbox;

	public ExecutableDataProvider(ParserToolbox parserToolbox) {
		this.parserToolbox = parserToolbox;
	}

	public CompletionSuggestions suggestMethods(String expectedName, List<ExecutableInfo> methodInfos, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			methodInfos,
			methodInfo -> new CompletionSuggestionMethod(methodInfo, insertionBegin, insertionEnd),
			rateMethodByNameAndTypesFunc(expectedName, expectation)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
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
			ParseExpectation argumentExpectation = ParseExpectationBuilder.expectObject().allowedTypes(expectedArgumentTypes_i).build();
			ParseResultIF argumentParseResult_i = parserToolbox.getRootParser().parse(tokenStream, parserToolbox.getThisInfo(), argumentExpectation);
			arguments.add(argumentParseResult_i);

			if (ParseUtils.propagateParseResult(argumentParseResult_i, argumentExpectation)) {
				return arguments;
			}

			ObjectParseResult parseResult = ((ObjectParseResult) argumentParseResult_i);
			int parsedToPosition = parseResult.getPosition();
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
					arguments.add(CompletionSuggestions.none(tokenStream.getPosition()));
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
		TypeToken<?> argumentType = parserToolbox.getObjectInfoProvider().getType(argInfo);
		return ParseUtils.isConvertibleTo(argumentType, expectedArgumentType);
	}

	public List<ExecutableInfo> getBestMatchingExecutableInfos(List<ExecutableInfo> availableExecutableInfos, List<ObjectInfo> argumentInfos) {
		ObjectInfoProvider objectInfoProvider = parserToolbox.getObjectInfoProvider();
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

	/*
	 * Suggestions
	 */
	private int rateMethodByName(ExecutableInfo methodInfo, String expectedName) {
		return ParseUtils.rateStringMatch(methodInfo.getName(), expectedName);
	}

	private int rateMethodByTypes(ExecutableInfo methodInfo, ParseExpectation expectation) {
		/*
		 * Even for EvaluationMode.DYNAMICALLY_TYPED we only consider the declared return type of the method instead
		 * of the runtime type of the returned object. Otherwise, we would have to invoke the method for code
		 * completion, possibly causing undesired side effects.
		 */
		List<TypeToken<?>> allowedTypes = expectation.getAllowedTypes();
		return	allowedTypes == null	? ParseUtils.TYPE_MATCH_FULL :
				allowedTypes.isEmpty()	? ParseUtils.TYPE_MATCH_NONE
										: allowedTypes.stream().mapToInt(allowedType -> ParseUtils.rateTypeMatch(methodInfo.getReturnType(), allowedType)).min().getAsInt();
	}

	private ToIntFunction<ExecutableInfo> rateMethodByNameAndTypesFunc(String methodName, ParseExpectation expectation) {
		return methodInfo -> (ParseUtils.TYPE_MATCH_NONE + 1)* rateMethodByName(methodInfo, methodName) + rateMethodByTypes(methodInfo, expectation);
	}

	public static String getMethodDisplayText(ExecutableInfo methodInfo) {
		return methodInfo.getName() + " (" + methodInfo.getDeclaringType() + ")";
	}
}
