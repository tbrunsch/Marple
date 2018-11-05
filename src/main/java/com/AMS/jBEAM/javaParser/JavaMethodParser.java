package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, Class<?> expectedResultClass) {
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
				ParseUtils.rateMethodByNameAndClassFunc(methodName, expectedResultClass)
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

		List<ParseResultIF> methodParseResults = new ArrayList<>();
		for (Method method : matchingMethods) {
			ParseResultIF parseResult = parseMethodArgumentList(tokenStream.clone(), currentContextInfo, method);
			methodParseResults.add(parseResult);
		}
		ParseResultIF methodParseResult = JavaParser.mergeParseResults(methodParseResults);
		switch (methodParseResult.getResultType()) {
			case COMPLETION_SUGGESTIONS:
				// code completion inside "()" => propagate completion suggestions
				return methodParseResult;
			case PARSE_ERROR:
				// always propagate errors
				return methodParseResult;
			case PARSE_RESULT: {
				ParseResult parseResult = (ParseResult) methodParseResult;
				int parsedToPosition = parseResult.getParsedToPosition();
				ObjectInfo objectInfo = parseResult.getObjectInfo();
				tokenStream.moveTo(parsedToPosition);
				return parserPool.getObjectTailParser().parse(tokenStream, objectInfo, expectedResultClass);
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + methodParseResult.getResultType());
		}
	}

	private ParseResultIF parseMethodArgumentList(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, Method method) {
		Class<?>[] argumentTypes = method.getParameterTypes();

		JavaToken characterToken = tokenStream.readCharacter();
		assert characterToken.getValue().equals("(");
		if (characterToken.isContainsCaret()) {
			if (argumentTypes.length == 0) {
				// no suggestions since method does not have arguments
				return new CompletionSuggestions(Collections.emptyMap());
			}
			return suggestFieldsAndMethodsForMethodArgument(tokenStream, argumentTypes[0]);
		}

		ObjectInfo[] argumentInfos = new ObjectInfo[argumentTypes.length];
		for (int i = 0; i < argumentTypes.length; i++) {
			if (i > 0) {
				int position = tokenStream.getPosition();
				if (!tokenStream.hasMore() || !(characterToken = tokenStream.readCharacter()).getValue().equals(",")) {
					return new ParseError(position, "Expected comma ','");
				}

				if (characterToken.isContainsCaret()) {
					return suggestFieldsAndMethodsForMethodArgument(tokenStream, argumentTypes[i]);
				}
			}

			ParseResultIF argumentParseResult = parserPool.getExpressionParser().parse(tokenStream, thisInfo, argumentTypes[i]);
			switch (argumentParseResult.getResultType()) {
				case COMPLETION_SUGGESTIONS:
					// code completion inside "[]" => propagate completion suggestions
					return argumentParseResult;
				case PARSE_ERROR:
					// always propagate errors
					return argumentParseResult;
				case PARSE_RESULT: {
					ParseResult parseResult = ((ParseResult) argumentParseResult);
					int parsedToPosition = parseResult.getParsedToPosition();
					tokenStream.moveTo(parsedToPosition);
					ObjectInfo argumentInfo = parseResult.getObjectInfo();
					argumentInfos[i] = argumentInfo;
					break;
				}
				default:
					throw new IllegalStateException("Unsupported parse result type: " + argumentParseResult.getResultType());
			}
		}

		int position = tokenStream.getPosition();
		if (!tokenStream.hasMore() || !(characterToken = tokenStream.readCharacter()).getValue().equals(")")) {
			return new ParseError(position, "Expected closing parenthesis ')'");
		}

		if (characterToken.isContainsCaret()) {
			// nothing we can suggest after ')'
			return new CompletionSuggestions(Collections.emptyMap());
		}

		// finished parsing
		ObjectInfo methodReturnInfo = getMethodReturnInfo(currentContextInfo, method, argumentInfos);

		// delegate parse result with corrected position (includes ')')
		return new ParseResult(tokenStream.getPosition(), methodReturnInfo);
	}

	private CompletionSuggestions suggestFieldsAndMethodsForMethodArgument(JavaTokenStream tokenStream, Class<?> argumentClass) {
		int insertionBegin, insertionEnd;
		insertionBegin = insertionEnd = tokenStream.getPosition();
		return suggestFieldsAndMethods(thisInfo, argumentClass, insertionBegin, insertionEnd);
	}
}
