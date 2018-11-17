package com.AMS.jBEAM.javaParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses a sub expression following a complete Java expression, assuming the context
 * <ul>
 *     <li>{@code <object>.} or</li>
 *     <li>{@code <object>[]</li>
 * </ul>
 */
class JavaObjectTailParser extends AbstractJavaEntityParser
{
    JavaObjectTailParser(JavaParserPool parserSettings, ObjectInfo thisInfo) {
        super(parserSettings, thisInfo);
    }

    @Override
    ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
        if (tokenStream.hasMore()) {
            char nextChar = tokenStream.peekCharacter();
            if (nextChar == '.') {
            	return parseDot(tokenStream, currentContextInfo, expectedResultClasses);
            } else if (nextChar == '[') {
                Class<?> elementClass = getClass(currentContextInfo).getComponentType();
                if (elementClass == null) {
                    // no array type
                    return new ParseError(tokenStream.getPosition(), "Cannot apply [] to non-array types");
                }
                ParseResultIF arrayIndexParseResult = parseArrayIndex(tokenStream);
                switch (arrayIndexParseResult.getResultType()) {
                    case COMPLETION_SUGGESTIONS:
                        // code completion inside "[]" => propagate completion suggestions
                        return arrayIndexParseResult;
                    case PARSE_ERROR:
					case AMBIGUOUS_PARSE_RESULT:
                        // always propagate errors
                        return arrayIndexParseResult;
                    case PARSE_RESULT: {
						ParseResult parseResult = (ParseResult) arrayIndexParseResult;
						int parsedToPosition = parseResult.getParsedToPosition();
						ObjectInfo indexInfo = parseResult.getObjectInfo();
						ObjectInfo elementInfo = getArrayElementInfo(currentContextInfo, indexInfo);
                        tokenStream.moveTo(parsedToPosition);
                        return parserPool.getObjectTailParser().parse(tokenStream, elementInfo, expectedResultClasses);
                    }
                    default:
                        throw new IllegalStateException("Unsupported parse result type: " + arrayIndexParseResult.getResultType());
                }
            } else if (nextChar == '(') {
            	/*
            	 * Prevent "<field>(" from being parsed as <field>. Otherwise, the expression
            	 * "<field>()" will be ambiguous if there also exists a method with the name of <field> and no arguments.
            	 */
            	return new ParseError(tokenStream.getPosition() + 1, "Unexpected opening parenthesis '('");
			}
        }
        // finished parsing
		if (expectedResultClasses != null
				&& expectedResultClasses.stream().noneMatch(expectedResultClass -> ParseUtils.isConvertibleTo(getClass(currentContextInfo), expectedResultClass))) {
			return new ParseError(tokenStream.getPosition(), "The class '" + getClass(currentContextInfo).getSimpleName() + "' cannot be casted to any of the expected class '" + expectedResultClasses.stream().map(clazz -> clazz.getSimpleName()).collect(Collectors.joining("', '")) + "'");
		}

        return new ParseResult(tokenStream.getPosition(), currentContextInfo);
    }

    private ParseResultIF parseDot(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		JavaToken characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals(".");
		if (characterToken.isContainsCaret()) {
			int insertionBegin = tokenStream.getPosition();
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (JavaTokenStream.JavaTokenParseException e) {
				insertionEnd = insertionBegin;
			}
			return suggestFieldsAndMethods(currentContextInfo, expectedResultClasses, insertionBegin, insertionEnd);
		}

		return JavaParser.parse(tokenStream, currentContextInfo, expectedResultClasses,
				parserPool.getFieldParser(false),
				parserPool.getMethodParser(false)
		);
	}

	private ParseResultIF parseArrayIndex(JavaTokenStream tokenStream) {
		List<Class<?>> expectedResultClasses = Arrays.asList(int.class);
		JavaToken characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");
		if (characterToken.isContainsCaret()) {
			int insertionBegin, insertionEnd;
			insertionBegin = insertionEnd = tokenStream.getPosition();
			return suggestFieldsAndMethods(thisInfo, expectedResultClasses, insertionBegin, insertionEnd);
		}

		ParseResultIF arrayIndexParseResult = parserPool.getExpressionParser().parse(tokenStream, thisInfo, expectedResultClasses);
		switch (arrayIndexParseResult.getResultType()) {
			case COMPLETION_SUGGESTIONS:
				// code completion inside "[]" => propagate completion suggestions
				return arrayIndexParseResult;
			case PARSE_ERROR:
			case AMBIGUOUS_PARSE_RESULT:
				// always propagate errors
				return arrayIndexParseResult;
			case PARSE_RESULT: {
				ParseResult parseResult = ((ParseResult) arrayIndexParseResult);
				int parsedToPosition = parseResult.getParsedToPosition();

				tokenStream.moveTo(parsedToPosition);
				characterToken = tokenStream.readCharacterUnchecked();

				if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
					return new ParseError(parsedToPosition, "Expected closing bracket ']'");
				}

				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after ']'
					return CompletionSuggestions.NONE;
				}

				// delegate parse result with corrected position (includes ']')
				return new ParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + arrayIndexParseResult.getResultType());
		}
	}
}
