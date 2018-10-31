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
                ParseResultIF arrayIndexParseResult = parseArrayIndex(tokenStream, thisContextClass);
                switch (arrayIndexParseResult.getResultType()) {
                    case COMPLETION_SUGGESTIONS:
                        // code completion inside "[]" => propagate completion suggestions
                        return arrayIndexParseResult;
                    case PARSE_ERROR:
                        // always propagate errors
                        return arrayIndexParseResult;
                    case PARSE_RESULT: {
                        int parsedToPosition = ((ParseResult) arrayIndexParseResult).getParsedToPosition();
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
		if (characterToken.isContainsCaret()) {
			int insertionBegin = tokenStream.getPosition();
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (JavaTokenStream.JavaTokenParseException e) {
				insertionEnd = insertionBegin;
			}

			List<CompletionSuggestion> suggestions = new ArrayList<>();

			List<Field> fields = parserSettings.getInspectionDataProvider().getFields(currentContextClass, false);
			suggestions.addAll(ParseUtils.createSuggestions(
				fields,
				ParseUtils.fieldTextInsertionInfoFunction(insertionBegin, insertionEnd),
				ParseUtils.FIELD_DISPLAY_FUNC,
				ParseUtils.rateFieldByNameAndClassFunc("", expectedResultClass))
			);

			List<Method> methods = parserSettings.getInspectionDataProvider().getMethods(currentContextClass, false);
			suggestions.addAll(ParseUtils.createSuggestions(
				methods,
				ParseUtils.methodTextInsertionInfoFunction(insertionBegin, insertionEnd),
				ParseUtils.METHOD_DISPLAY_FUNC,
				ParseUtils.rateMethodByNameAndClassFunc("", expectedResultClass))
			);
			return new CompletionSuggestions(suggestions);
		}

		return JavaParser.parse(tokenStream, currentContextClass, expectedResultClass,
				parserSettings.getFieldParser(false)//,
				// TODO: Add method parser
				//parserSettings.getMethodParser(staticOnly)
		);
	}

	private ParseResultIF parseArrayIndex(JavaTokenStream tokenStream, Class<?> currentContextClass) {
		Class<?> expectedResultClass = int.class;
		JavaToken characterToken = tokenStream.readCharacter();
		if (characterToken.isContainsCaret()) {
			// Suggest all fields and methods of this' class, preferring integers (for index)
			List<CompletionSuggestion> suggestions = new ArrayList<>();

			List<Field> fields = parserSettings.getInspectionDataProvider().getFields(thisContextClass, false);
			suggestions.addAll(ParseUtils.createSuggestions(
				fields,
				ParseUtils.fieldTextInsertionInfoFunction(tokenStream.getPosition(), tokenStream.getPosition()),
				ParseUtils.FIELD_DISPLAY_FUNC,
				ParseUtils.rateFieldByClassFunc(expectedResultClass))
			);

			List<Method> methods = parserSettings.getInspectionDataProvider().getMethods(thisContextClass, false);
			suggestions.addAll(ParseUtils.createSuggestions(
				methods,
				ParseUtils.methodTextInsertionInfoFunction(tokenStream.getPosition(), tokenStream.getPosition()),
				ParseUtils.METHOD_DISPLAY_FUNC,
				ParseUtils.rateMethodByClassFunc(expectedResultClass))
			);

			return new CompletionSuggestions(suggestions);
		}

		ParseResultIF arrayIndexParseResult = parserSettings.getExpressionParser().parse(tokenStream, currentContextClass, expectedResultClass);
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
				characterToken = tokenStream.readCharacter();
				if (!characterToken.getValue().equals("]")) {
					return new ParseError(tokenStream.getPosition(), "Expected closing bracket ']'");
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
