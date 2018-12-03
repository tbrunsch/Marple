package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.JavaParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FieldAndMethodDataProvider
{
	private final JavaParserContext parserContext;

	public FieldAndMethodDataProvider(JavaParserContext parserContext) {
		this.parserContext = parserContext;
	}

	public CompletionSuggestions suggestFieldsAndMethods(TokenStream tokenStream, List<Class<?>> expectedClasses) {
		ObjectInfo thisInfo = parserContext.getThisInfo();
		if (thisInfo.getObject() == null) {
			return CompletionSuggestions.NONE;
		}
		int insertionBegin, insertionEnd;
		insertionBegin = insertionEnd = tokenStream.getPosition();
		return suggestFieldsAndMethods(thisInfo, expectedClasses, insertionBegin, insertionEnd, false);
	}

	public CompletionSuggestions suggestFieldsAndMethods(ObjectInfo contextInfo, List<Class<?>> expectedClasses, final int insertionBegin, final int insertionEnd, boolean staticOnly) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = new LinkedHashMap<>();

		Class<?> contextClass = parserContext.getObjectInfoProvider().getClass(contextInfo);

		List<Field> fields = parserContext.getInspectionDataProvider().getFields(contextClass, staticOnly);
		ratedSuggestions.putAll(ParseUtils.createRatedSuggestions(
				fields,
				field -> new CompletionSuggestionField(field, insertionBegin, insertionEnd),
				ParseUtils.rateFieldByClassesFunc(expectedClasses))
		);

		List<Method> methods = parserContext.getInspectionDataProvider().getMethods(contextClass, staticOnly);
		ratedSuggestions.putAll(ParseUtils.createRatedSuggestions(
				methods,
				method -> new CompletionSuggestionMethod(method, insertionBegin, insertionEnd),
				ParseUtils.rateMethodByClassesFunc(expectedClasses))
		);

		return new CompletionSuggestions(ratedSuggestions);
	}

	public List<ParseResultIF> parseMethodArguments(TokenStream tokenStream, List<? extends Executable> availableMethods) {
		List<ParseResultIF> methodArguments = new ArrayList<>();

		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		boolean requestedCodeCompletionBeforeNextArgument = characterToken.isContainsCaret();

		assert characterToken.getValue().charAt(0) == '(';

		// The case requestedCodeCompletionBeforeNextArgument == true will be handled at the beginning of the first loop iteration
		if (!requestedCodeCompletionBeforeNextArgument) {
			if (!tokenStream.hasMore()) {
				methodArguments.add(new ParseError(tokenStream.getPosition(), "Expected argument or closing parenthesis ')'", ParseError.ErrorType.SYNTAX_ERROR));
				return methodArguments;
			}

			char nextCharacter = tokenStream.peekCharacter();
			if (nextCharacter == ')') {
				tokenStream.readCharacterUnchecked();
				return methodArguments;
			}
		}

		for (int argIndex = 0; ; argIndex++) {
			final int i = argIndex;

			availableMethods = availableMethods.stream().filter(method -> isArgumentIndexValid(method, i)).collect(Collectors.toList());
			List<Class<?>> expectedArgumentTypes_i = availableMethods.stream().map(method -> method.getParameterTypes()[i]).distinct().collect(Collectors.toList());

			if (expectedArgumentTypes_i.isEmpty()) {
				methodArguments.add(new ParseError(tokenStream.getPosition(), "No further arguments expected", ParseError.ErrorType.SEMANTIC_ERROR));
				return methodArguments;
			}

			if (requestedCodeCompletionBeforeNextArgument) {
				// suggestions for argument i
				if (availableMethods.isEmpty()) {
					// no suggestions since no further arguments expected
					methodArguments.add(CompletionSuggestions.NONE);
				} else {
					methodArguments.add(suggestFieldsAndMethods(tokenStream, expectedArgumentTypes_i));
				}
				return methodArguments;
			}

			/*
			 * Parse expression for argument i
			 */
			ParseResultIF argumentParseResult_i = parserContext.getCompoundExpressionParser().parse(tokenStream, parserContext.getThisInfo(), expectedArgumentTypes_i);
			methodArguments.add(argumentParseResult_i);

			// always stop except for results
			if (argumentParseResult_i.getResultType() != ParseResultType.PARSE_RESULT) {
				return methodArguments;
			}

			ParseResult parseResult = ((ParseResult) argumentParseResult_i);
			int parsedToPosition = parseResult.getParsedToPosition();
			tokenStream.moveTo(parsedToPosition);
			ObjectInfo argumentInfo = parseResult.getObjectInfo();
			availableMethods = availableMethods.stream().filter(method -> acceptsArgumentInfo(method, i, argumentInfo)).collect(Collectors.toList());

			position = tokenStream.getPosition();
			characterToken = tokenStream.readCharacterUnchecked();

			if (characterToken == null) {
				methodArguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'", ParseError.ErrorType.SYNTAX_ERROR));
				return methodArguments;
			}

			if (characterToken.getValue().charAt(0) == ')') {
				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after ')'
					methodArguments.add(CompletionSuggestions.NONE);
				}
				return methodArguments;
			}

			if (characterToken.getValue().charAt(0) != ',') {
				methodArguments.add(new ParseError(position, "Expected comma ',' or closing parenthesis ')'", ParseError.ErrorType.SYNTAX_ERROR));
				return methodArguments;
			}

			requestedCodeCompletionBeforeNextArgument = characterToken.isContainsCaret();
		}
	}

	private static boolean isArgumentIndexValid(Executable method, int argIndex) {
		return method.isVarArgs() || method.getParameterCount() > argIndex;
	}

	private boolean acceptsArgumentInfo(Executable method, int argIndex, ObjectInfo argInfo) {
		final Class<?> expectedArgumentType;
		int numArguments = method.getParameterCount();
		if (argIndex < numArguments) {
			expectedArgumentType = method.getParameterTypes()[argIndex];
		} else {
			if (method.isVarArgs()) {
				expectedArgumentType = method.getParameterTypes()[numArguments - 1];
			} else {
				return false;
			}
		}
		Class<?> argClass = parserContext.getObjectInfoProvider().getClass(argInfo);
		return ParseUtils.isConvertibleTo(argClass, expectedArgumentType);
	}

	public <T extends Executable> List<T> getBestMatchingMethods(List<T> availableMethods, List<ObjectInfo> argumentInfos) {
		int[] methodMatchRating = availableMethods.stream()
				.mapToInt(method -> rateArgumentMatch(method, argumentInfos))
				.toArray();

		List<T> methods;

		int[][] allowedRatingsByPhase = {
				{ ParseUtils.CLASS_MATCH_FULL },
				{ ParseUtils.CLASS_MATCH_INHERITANCE, ParseUtils.CLASS_MATCH_PRIMITIVE_CONVERSION},
				{ ParseUtils.CLASS_MATCH_BOXED, ParseUtils.CLASS_MATCH_BOXED_AND_CONVERSION, ParseUtils.CLASS_MATCH_BOXED_AND_INHERITANCE }
		};

		for (boolean allowVariadicMethods : Arrays.asList(false, true)) {
			for (int[] allowedRatings : allowedRatingsByPhase) {
				methods = filterMethods(availableMethods, allowedRatings, allowVariadicMethods, methodMatchRating);
				if (!methods.isEmpty()) {
					return methods;
				}
			}
		}
		return Collections.emptyList();
	}

	private int rateArgumentMatch(Executable method, List<ObjectInfo> argumentInfos) {
		List<Class<?>> argumentTypes = Arrays.stream(method.getParameterTypes()).collect(Collectors.toList());

		if (method.isVarArgs()) {
			// adapt number of argument types
			int deltaNumArguments = argumentInfos.size() - argumentTypes.size();
			if (deltaNumArguments < -1) {
				return ParseUtils.CLASS_MATCH_NONE;
			} else if (deltaNumArguments == -1) {
				// variadic argument can be omitted
				argumentTypes = argumentTypes.subList(0, argumentTypes.size());
			} else {
				// variable number of variadic arguments
				int lastArgumentIndex = argumentTypes.size() - 1;
				assert lastArgumentIndex >= 0 : "Variadic methods have at least one declared argument type";
				Class<?> variadicArgumentType = argumentTypes.get(lastArgumentIndex).getComponentType();
				assert variadicArgumentType != null : "Last argument type of variadic methods is an array";
				argumentTypes.set(lastArgumentIndex, variadicArgumentType);
				while (argumentTypes.size() < argumentInfos.size()) {
					argumentTypes.add(variadicArgumentType);
				}
			}
		} else {
			if (argumentTypes.size() != argumentInfos.size()) {
				return ParseUtils.CLASS_MATCH_NONE;
			}
		}
		assert argumentTypes.size() == argumentInfos.size() : "Adaption of argument types failed";

		int worstArgumentClassMatchRating = ParseUtils.CLASS_MATCH_FULL;
		for (int i = 0; i < argumentInfos.size(); i++) {
			Class<?> expectedType = argumentTypes.get(i);
			Class<?> actualType = parserContext.getObjectInfoProvider().getClass(argumentInfos.get(i));
			int argumentClassMatchRating = ParseUtils.rateClassMatch(actualType, expectedType);
			worstArgumentClassMatchRating = Math.max(worstArgumentClassMatchRating, argumentClassMatchRating);
		}
		return worstArgumentClassMatchRating;
	}

	private static <T extends Executable> List<T> filterMethods(List<T> methods, int[] allowedRatings, boolean allowVariadicMethods, int[] methodMatchRating) {
		List<T> filteredMethods = new ArrayList<>();
		for (int i = 0; i < methods.size(); i++) {
			int rating = methodMatchRating[i];
			if (IntStream.of(allowedRatings).noneMatch(allowedRating -> rating == allowedRating)) {
				continue;
			}
			T method = methods.get(i);
			if (!allowVariadicMethods && method.isVarArgs()) {
				continue;
			}
			filteredMethods.add(method);
		}
		return filteredMethods;
	}

}
