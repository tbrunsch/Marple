package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses a sub expression following a complete Java expression, assuming the context
 * <ul>
 *     <li>{@code <object>.} or</li>
 *     <li>{@code <object>[]</li>
 * </ul>
 */
class JavaObjectTailParser extends AbstractJavaEntityParser
{
    JavaObjectTailParser(JavaParserSettings parserSettings, Class<?> thisContextClass) {
        super(parserSettings, thisContextClass);
    }

    @Override
    ParseResultIF doParse(JavaTokenStream tokenStream, Class<?> currentContextClass, Class<?> expectedResultClass) {
        if (tokenStream.hasMore()) {
            char nextChar = tokenStream.peekCharacter();
            if (nextChar == '.') {
            	return parseDot(tokenStream, currentContextClass, expectedResultClass);
            } else if (nextChar == '[') {
                Class<?> elementClass = currentContextClass.getComponentType();
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
                        // always propagate errors
                        return arrayIndexParseResult;
                    case PARSE_RESULT: {
						ParseResult parseResult = (ParseResult) arrayIndexParseResult;
						int parsedToPosition = parseResult.getParsedToPosition();
                        tokenStream.moveTo(parsedToPosition);
                        return parserSettings.getObjectTailParser().parse(tokenStream, elementClass, expectedResultClass);
                    }
                    default:
                        throw new IllegalStateException("Unsupported parse result type: " + arrayIndexParseResult.getResultType());
                }
            }
        }
        // finished parsing
		if (expectedResultClass != null && !ParseUtils.isConvertibleTo(currentContextClass, expectedResultClass)) {
			return new ParseError(tokenStream.getPosition(), "The class '" + currentContextClass.getSimpleName() + "' cannot be casted to the expected class '" + expectedResultClass.getSimpleName() + "");
		}

		// TODO: Determine real object when doing real parsing
        return new ParseResult(tokenStream.getPosition(), new ObjectInfo(null, currentContextClass));
    }

    private ParseResultIF parseDot(JavaTokenStream tokenStream, Class<?> currentContextClass, Class<?> expectedResultClass) {
		JavaToken characterToken = tokenStream.readCharacter();
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
			return suggestFieldsAndMethods(currentContextClass, expectedResultClass, insertionBegin, insertionEnd);
		}

		return JavaParser.parse(tokenStream, currentContextClass, expectedResultClass,
				parserSettings.getFieldParser(false),
				parserSettings.getMethodParser(false)
		);
	}

	private ParseResultIF parseArrayIndex(JavaTokenStream tokenStream) {
		Class<?> expectedResultClass = int.class;
		JavaToken characterToken = tokenStream.readCharacter();
		assert characterToken.getValue().equals("[");
		if (characterToken.isContainsCaret()) {
			int insertionBegin, insertionEnd;
			insertionBegin = insertionEnd = tokenStream.getPosition();
			return suggestFieldsAndMethods(thisContextClass, expectedResultClass, insertionBegin, insertionEnd);
		}

		ParseResultIF arrayIndexParseResult = parserSettings.getExpressionParser().parse(tokenStream, thisContextClass, expectedResultClass);
		switch (arrayIndexParseResult.getResultType()) {
			case COMPLETION_SUGGESTIONS:
				// code completion inside "[]" => propagate completion suggestions
				return arrayIndexParseResult;
			case PARSE_ERROR:
				// always propagate errors
				return arrayIndexParseResult;
			case PARSE_RESULT: {
				ParseResult parseResult = ((ParseResult) arrayIndexParseResult);
				int parsedToPosition = parseResult.getParsedToPosition();

				tokenStream.moveTo(parsedToPosition);
				if (!tokenStream.hasMore() || !(characterToken = tokenStream.readCharacter()).getValue().equals("]")) {
					return new ParseError(parsedToPosition, "Expected closing bracket ']'");
				}

				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after ']'
					return new CompletionSuggestions(Collections.emptyList());
				}

				// delegate parse result with corrected position (includes ']')
				return new ParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + arrayIndexParseResult.getResultType());
		}
	}
}
