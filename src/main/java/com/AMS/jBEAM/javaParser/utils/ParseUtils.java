package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.parsers.AbstractEntityParser;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.google.common.reflect.TypeToken;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class ParseUtils
{
	/*
	 * Parsing
	 */
	public static ParseResultIF parse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes, AbstractEntityParser... parsers) {
		List<ParseResultIF> parseResults = Arrays.stream(parsers)
			.map(parser -> parser.parse(tokenStream, currentContextInfo, expectedResultTypes))
			.collect(Collectors.toList());
		return mergeParseResults(parseResults);
	}

	private static ParseResultIF mergeParseResults(List<ParseResultIF> parseResults) {
		if (parseResults.isEmpty()) {
			throw new IllegalArgumentException("Cannot merge 0 parse results");
		}

		List<AmbiguousParseResult> ambiguousResults = filterParseResults(parseResults, AmbiguousParseResult.class);
		List<ParseResult> results = filterParseResults(parseResults, ParseResult.class);
		List<CompletionSuggestions> completionSuggestions = filterParseResults(parseResults, CompletionSuggestions.class);
		List<ParseError> errors = filterParseResults(parseResults, ParseError.class);

		if (!completionSuggestions.isEmpty()) {
			// Merge and return suggestions
			Map<CompletionSuggestionIF, Integer> mergedRatedSuggestions = new LinkedHashMap<>();
			for (CompletionSuggestions suggestions : completionSuggestions) {
				Map<CompletionSuggestionIF, Integer> ratedSuggestions = suggestions.getRatedSuggestions();
				for (CompletionSuggestionIF suggestion : ratedSuggestions.keySet()) {
					int currentRating = mergedRatedSuggestions.containsKey(suggestion)
							? mergedRatedSuggestions.get(suggestion)
							: Integer.MAX_VALUE;
					int newRating = ratedSuggestions.get(suggestion);
					int bestRating = Math.min(currentRating, newRating);
					mergedRatedSuggestions.put(suggestion, bestRating);
				}
			}
			return new CompletionSuggestions(mergedRatedSuggestions);
		}

		boolean ambiguous = !ambiguousResults.isEmpty() || results.size() > 1;
		if (ambiguous) {
			int position = ambiguousResults.isEmpty() ? results.get(0).getParsedToPosition() : ambiguousResults.get(0).getPosition();
			String message = "Ambiguous expression:\n"
					+ ambiguousResults.stream().map(AmbiguousParseResult::getMessage).collect(Collectors.joining("\n"))
					+ (ambiguousResults.size() > 0 && results.size() > 0 ? "\n" : "")
					+ results.stream().map(result -> "Expression can be evaluated to object of type " + result.getObjectInfo().getDeclaredType()).collect(Collectors.joining("\n"));
			return new AmbiguousParseResult(position, message);
		}

		if (results.size() == 1) {
			return results.get(0);
		}

		if (errors.size() > 1) {
			return mergeParseErrors(errors);
		} else {
			return errors.get(0);
		}
	}

	private static <T> List<T> filterParseResults(List<ParseResultIF> parseResults, Class<T> filterClass) {
		return parseResults.stream().filter(filterClass::isInstance).map(filterClass::cast).collect(Collectors.toList());
	}

	private static ParseError mergeParseErrors(List<ParseError> errors) {
		for (ParseError.ErrorType errorType : ParseError.ErrorType.values()) {
			List<ParseError> errorsOfCurrentType = errors.stream().filter(error -> error.getErrorType() == errorType).collect(Collectors.toList());
			if (errorsOfCurrentType.isEmpty()) {
				continue;
			}
			if (errorsOfCurrentType.size() == 1) {
				return errorsOfCurrentType.get(0);
			}
			/*
			 * Heuristic: Only consider errors with maximum position. These are probably
			 *            errors of parsers that are most likely supposed to match.
			 */
			int maxPosition = errorsOfCurrentType.stream().mapToInt(ParseError::getPosition).max().getAsInt();
			String message = errorsOfCurrentType.stream()
								.filter(error -> error.getPosition() == maxPosition)
								.map(ParseError::getMessage)
								.collect(Collectors.joining("\n"));
			return new ParseError(maxPosition, message, errorType);
		}
		return new ParseError(-1, "Internal error: Failed merging parse errors", ParseError.ErrorType.INTERNAL_ERROR);
	}

	public static ParseResultIF createParseResult(ParserContext parserContext, ObjectInfo resultInfo, List<TypeToken<?>> expectedResultTypes, int parsedToPosition) {
		TypeToken<?> resultType = parserContext.getObjectInfoProvider().getType(resultInfo);
		if (expectedResultTypes != null && expectedResultTypes.stream().noneMatch(expectedResultType -> ParseUtils.isConvertibleTo(resultType, expectedResultType))) {
			String messagePrefix = "The class '" + resultType + "' is not assignable to ";
			String messageMiddle = expectedResultTypes.size() > 1
					? "any of the expected classes "
					: "the expected class ";
			String messageSuffix = "'" + expectedResultTypes.stream().map(Object::toString).collect(Collectors.joining("', '")) + "'";

			return new ParseError(parsedToPosition, messagePrefix + messageMiddle + messageSuffix, ParseError.ErrorType.SEMANTIC_ERROR);
		}

		return new ParseResult(parsedToPosition, resultInfo);
	}

	/*
	 * String Comparison
	 */
	private static final int STRING_MATCH_FULL							= 0;
	private static final int STRING_MATCH_FULL_IGNORE_CASE				= 1;
	private static final int STRING_MATCH_PREFIX						= 2;
	private static final int STRING_MATCH_PREFIX_IGNORE_CASE			= 3;
	private static final int STRING_MATCH_INVERSE_PREFIX				= 4;
	private static final int STRING_MATCH_INVERSE_PREFIX_IGNORE_CASE	= 5;
	private static final int STRING_MATCH_MIN_VALUE_OTHER				= 6;

	public static int rateStringMatch(String actual, String expected) {
		if (actual.equals(expected)) {
			return STRING_MATCH_FULL;
		} else {
			String actualLowerCase = actual.toLowerCase();
			String expectedLowerCase = expected.toLowerCase();
			if (actualLowerCase.equals(expectedLowerCase)) {
				return STRING_MATCH_FULL_IGNORE_CASE;
			} else if (actual.startsWith(expected)) {
				return STRING_MATCH_PREFIX;
			} else if (actualLowerCase.startsWith(expectedLowerCase)) {
				return STRING_MATCH_PREFIX_IGNORE_CASE;
			} else if (expected.startsWith(actual)) {
				return STRING_MATCH_INVERSE_PREFIX;
			} else if (expectedLowerCase.startsWith(actualLowerCase)) {
				return STRING_MATCH_INVERSE_PREFIX_IGNORE_CASE;
			} else {
				// TODO: Differentiate between Strings
				return STRING_MATCH_MIN_VALUE_OTHER;
			}
		}
	}

	/*
	 * Type Comparison
	 */
	static final int TYPE_MATCH_FULL					= 0;
	static final int TYPE_MATCH_INHERITANCE				= 1;
	static final int TYPE_MATCH_PRIMITIVE_CONVERSION	= 2;
	static final int TYPE_MATCH_BOXED					= 3;
	static final int TYPE_MATCH_BOXED_AND_CONVERSION	= 4;
	static final int TYPE_MATCH_BOXED_AND_INHERITANCE	= 5;
	static final int TYPE_MATCH_NONE					= 6;

	static int rateTypeMatch(TypeToken<?> actual, TypeToken<?> expected) {
		if (expected == null) {
			// no expectations
			return TYPE_MATCH_FULL;
		}

		if (actual == null) {
			// null object (only object without class) is convertible to any non-primitive class
			return expected.isPrimitive() ? TYPE_MATCH_NONE : TYPE_MATCH_FULL;
		}

		if (actual.equals(expected)) {
			return TYPE_MATCH_FULL;
		}

		Class<?> actualClass = actual.getRawType();
		Class<?> expectedClass = expected.getRawType();
		boolean primitiveConvertible = ReflectionUtils.isPrimitiveConvertibleTo(actualClass, expectedClass, false);
		if (expected.isPrimitive()) {
			if (actual.isPrimitive()) {
				return primitiveConvertible
						? TYPE_MATCH_PRIMITIVE_CONVERSION    // int -> double
						: TYPE_MATCH_NONE;					// int -> boolean
			} else {
				Class<?> actualUnboxedClass = ReflectionUtils.getPrimitiveClass(actualClass);
				return	actualUnboxedClass == expectedClass	? TYPE_MATCH_BOXED :				// Integer -> int
						primitiveConvertible				? TYPE_MATCH_BOXED_AND_CONVERSION   // Integer -> double
															: TYPE_MATCH_NONE;					// Integer -> boolean
			}
		} else {
			if (actual.isPrimitive()) {
				Class<?> actualBoxedClass = ReflectionUtils.getBoxedClass(actualClass);
				return	actualBoxedClass == expectedClass					? TYPE_MATCH_BOXED :				// int -> Integer
						primitiveConvertible								? TYPE_MATCH_BOXED_AND_CONVERSION :	// int -> Double
						expectedClass.isAssignableFrom(actualBoxedClass)	? TYPE_MATCH_BOXED_AND_INHERITANCE 	// int -> Number
																			: TYPE_MATCH_NONE;					// int -> String
			} else {
				return	expected.isSupertypeOf(actual)	? TYPE_MATCH_INHERITANCE    // Integer -> Number
														: TYPE_MATCH_NONE;			// Integer -> Double
			}
		}
	}

	public static boolean isConvertibleTo(TypeToken<?> source, TypeToken<?> target) {
		return rateTypeMatch(source, target) != TYPE_MATCH_NONE;
	}

	/*
	 * Completion Suggestions
	 */
	public static <T> Map<CompletionSuggestionIF, Integer> createRatedSuggestions(List<T> objects, Function<T, CompletionSuggestionIF> suggestionBuilder, ToIntFunction<T> ratingFunc) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = new LinkedHashMap<>();
		for (T object : objects) {
			CompletionSuggestionIF suggestion = suggestionBuilder.apply(object);
			int rating = ratingFunc.applyAsInt(object);
			ratedSuggestions.put(suggestion, rating);
		}
		return ratedSuggestions;
	}
}
