package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parses a sub expression starting with a method {@code <method>}, assuming the context
 * <ul>
 *     <li>{@code <context instance>.<method>},</li>
 *     <li>{@code <context class>.<method>}, or</li>
 *     <li>{@code <fmethod} (like {@code <context instance>.<method>} for {@code <context instance> = this})</li>
 * </ul>
 */
class JavaMethodParser extends AbstractJavaEntityParser
{
	private final boolean staticOnly;

	JavaMethodParser(JavaParserPool parserSettings, ObjectInfo thisInfo, boolean staticOnly) {
		super(parserSettings, thisInfo);
		this.staticOnly = staticOnly;
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		final int startPosition = tokenStream.getPosition();
		JavaToken methodNameToken;
		try {
			methodNameToken = tokenStream.readIdentifier();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected an identifier");
		}
		String methodName = methodNameToken.getValue();
		final int endPosition = tokenStream.getPosition();

		List<Method> methods = parserPool.getInspectionDataProvider().getMethods(getClass(currentContextInfo), staticOnly);

		// check for code completion
		if (methodNameToken.isContainsCaret()) {
			Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
				methods,
				method -> new CompletionSuggestionMethod(method, startPosition, endPosition),
				ParseUtils.rateMethodByNameAndClassesFunc(methodName, expectedResultClasses)
			);
			return new CompletionSuggestions(ratedSuggestions);
		}

		// no code completion requested => field name must exist
		List<Method> matchingMethods = methods.stream().filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
		if (matchingMethods.isEmpty()) {
			return new ParseError(startPosition, "Unknown method '" + methodName + "'");
		}

		if (!tokenStream.hasMore() || tokenStream.peekCharacter() != '(') {
			return new ParseError(tokenStream.getPosition(), "Expected opening parenthesis '('");
		}

		List<ParseResultIF> argumentParseResults = parseMethodArguments(tokenStream, matchingMethods);

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
		List<Method> bestMatchingMethods = getBestMatchingMethods(matchingMethods, argumentInfos);

		switch (bestMatchingMethods.size()) {
			case 0:
				return new ParseError(tokenStream.getPosition(), "No method matches the given arguments");
			case 1: {
				Method bestMatchingMethod = bestMatchingMethods.get(0);
				ObjectInfo methodReturnInfo = getMethodReturnInfo(currentContextInfo, bestMatchingMethod, argumentInfos);
				return parserPool.getObjectTailParser().parse(tokenStream, methodReturnInfo, expectedResultClasses);
			}
			default: {
				String error = "Ambiguous method call. Possible candidates are:\n"
								+ bestMatchingMethods.stream().map(JavaMethodParser::formatMethod).collect(Collectors.joining("\n"));
				return new AmbiguousParseResult(tokenStream.getPosition(), error);
			}
		}
	}

	private static String formatMethod(Method method) {
		return method.getName()
				+ "("
				+ Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "))
				+ ")";
	}
}
