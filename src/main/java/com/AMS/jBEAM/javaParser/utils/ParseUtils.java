package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.common.RegexUtils;
import com.AMS.jBEAM.javaParser.parsers.AbstractEntityParser;
import com.AMS.jBEAM.javaParser.parsers.ParseExpectation;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.google.common.reflect.TypeToken;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParseUtils
{
	/*
	 * Parsing
	 */
	public static <C> ParseResultIF parse(TokenStream tokenStream, C context, ParseExpectation expectation, AbstractEntityParser<? super C>... parsers) {
		List<ParseResultIF> parseResults = Arrays.stream(parsers)
			.map(parser -> parser.parse(tokenStream, context, expectation))
			.collect(Collectors.toList());
		return mergeParseResults(parseResults);
	}

	private static ParseResultIF mergeParseResults(List<ParseResultIF> parseResults) {
		if (parseResults.isEmpty()) {
			throw new IllegalArgumentException("Internal error: Cannot merge 0 parse results");
		}

		List<AmbiguousParseResult> ambiguousResults = filterParseResults(parseResults, AmbiguousParseResult.class);
		List<CompletionSuggestions> completionSuggestions = filterParseResults(parseResults, CompletionSuggestions.class);
		List<ParseError> errors = filterParseResults(parseResults, ParseError.class);
		List<ParseResultIF> results = new ArrayList<>();
		results.addAll(filterParseResults(parseResults, ObjectParseResult.class));
		results.addAll(filterParseResults(parseResults, ClassParseResult.class));

		if (!completionSuggestions.isEmpty()) {
			return mergeCompletionSuggestions(completionSuggestions);
		}

		boolean ambiguous = !ambiguousResults.isEmpty() || results.size() > 1;
		if (ambiguous) {
			return mergeResults(ambiguousResults, results);
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

	private static ParseResultIF mergeCompletionSuggestions(List<CompletionSuggestions> completionSuggestions) {
		Map<CompletionSuggestionIF, Integer> mergedRatedSuggestions = new LinkedHashMap<>();
		int position = Integer.MAX_VALUE;
		for (CompletionSuggestions suggestions : completionSuggestions) {
			position = Math.min(position, suggestions.getPosition());
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
		return new CompletionSuggestions(position, mergedRatedSuggestions);
	}

	private static ParseResultIF mergeResults(List<AmbiguousParseResult> ambiguousResults, List<ParseResultIF> results) {
		int position = ambiguousResults.isEmpty() ? results.get(0).getPosition() : ambiguousResults.get(0).getPosition();
		StringBuilder builder = new StringBuilder("Ambiguous expression:");
		for (AmbiguousParseResult ambiguousResult : ambiguousResults) {
			builder.append("\n").append(ambiguousResult.getMessage());
		}
		for (ParseResultIF result : results) {
			if (result instanceof ObjectParseResult) {
				builder.append("Expression can be evaluated to object of type ").append(((ObjectParseResult) result).getObjectInfo().getDeclaredType());
			} else if (result instanceof ClassParseResult) {
				builder.append("Expression can be evaluated to type ").append(((ClassParseResult) result).getType());
			} else {
				throw new IllegalArgumentException("Internal error: Expected an object or a class as parse result, but found " + result.getClass().getSimpleName());
			}
		}
		return new AmbiguousParseResult(position, builder.toString());
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

	/*
	 * String Comparison
	 */
	private static final int STRING_MATCH_FULL							= 0;
	private static final int STRING_MATCH_FULL_IGNORE_CASE				= 1;
	private static final int STRING_MATCH_PREFIX						= 2;
	private static final int STRING_MATCH_PREFIX_IGNORE_CASE			= 3;
	private static final int STRING_MATCH_INVERSE_PREFIX				= 4;
	private static final int STRING_MATCH_INVERSE_PREFIX_IGNORE_CASE	= 5;
	private static final int STRING_MATCH_WILDCARD = 6;
	private static final int STRING_MATCH_MIN_VALUE_OTHER				= 7;

	public static int rateStringMatch(String actual, String expected) {
		if (actual.equals(expected)) {
			return STRING_MATCH_FULL;
		} else if (expected.isEmpty()) {
			return actual.isEmpty() ? STRING_MATCH_FULL : STRING_MATCH_PREFIX;
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
			} else if (WildcardPatternGenerator.generate(expected).matcher(actual).matches()) {
				return STRING_MATCH_WILDCARD;
			} else {
				return STRING_MATCH_MIN_VALUE_OTHER;
			}
		}
	}

	/*
	 * Type Comparison
	 */
	public static final int	TYPE_MATCH_FULL						= 0;
	public static final int	TYPE_MATCH_INHERITANCE				= 1;
	public static final int	TYPE_MATCH_PRIMITIVE_CONVERSION		= 2;
	public static final int	TYPE_MATCH_BOXED					= 3;
	public static final int	TYPE_MATCH_BOXED_AND_CONVERSION		= 4;
	public static final int	TYPE_MATCH_BOXED_AND_INHERITANCE	= 5;
	public static final int	TYPE_MATCH_NONE						= 6;

	public static int rateTypeMatch(TypeToken<?> actual, TypeToken<?> expected) {
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
	public static <T> Map<CompletionSuggestionIF, Integer> createRatedSuggestions(Collection<T> objects, Function<T, CompletionSuggestionIF> suggestionBuilder, ToIntFunction<T> ratingFunc) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = new LinkedHashMap<>();
		for (T object : objects) {
			CompletionSuggestionIF suggestion = suggestionBuilder.apply(object);
			int rating = ratingFunc.applyAsInt(object);
			ratedSuggestions.put(suggestion, rating);
		}
		return ratedSuggestions;
	}

	/**
	 * Throws an IllegalArgumentException if the parse result is an object parse result when a class parse
	 * result is expected or vice versa.
	 *
	 * Returns true if the parse result is not of the expected type. This is the case for completion suggestions,
	 * errors, and ambiguous parse results.
	 */
	public static boolean propagateParseResult(ParseResultIF parseResult, ParseExpectation expectation) {
		ParseResultType parseResultType = parseResult.getResultType();
		ParseResultType expectedEvaluationType = expectation.getEvaluationType();
		if (expectedEvaluationType == ParseResultType.OBJECT_PARSE_RESULT && parseResultType == ParseResultType.CLASS_PARSE_RESULT) {
			throw new IllegalStateException("Internal error: Expected an object as parse result, but obtained a class");
		}
		if (expectedEvaluationType == ParseResultType.CLASS_PARSE_RESULT && parseResultType == ParseResultType.OBJECT_PARSE_RESULT) {
			throw new IllegalStateException("Internal error: Expected a class as parse result, but obtained an object");
		}
		return parseResultType != expectedEvaluationType;
	}

	private static class WildcardPatternGenerator
	{
		private static String	WILDCARD_STRING;	// caches last wildcard string
		private static Pattern	PATTERN;

		static Pattern generate(String wildcardString) {
			if (!Objects.equals(wildcardString, WILDCARD_STRING)) {
				WILDCARD_STRING = wildcardString;
				PATTERN = RegexUtils.createRegexForWildcardString(wildcardString);
			}
			return PATTERN;
		}
	}
}
