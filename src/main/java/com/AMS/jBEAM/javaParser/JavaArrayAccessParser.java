package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses a sub expression embraced by square brackets, assuming the context
 * {@code <array>[<integer expression>]}
 */
class JavaArrayAccessParser extends AbstractJavaEntityParser
{
    JavaArrayAccessParser(JavaParserSettings parserSettings, Class<?> thisContextClass) {
        super(parserSettings, thisContextClass);
    }

    @Override
    ParseResultIF doParse(JavaTokenStream tokenStream, Class<?> currentContextClass, Class<?> expectedResultClass) {
    	assert expectedResultClass == int.class : "Array index must be an int or convertible to int";

        int position = tokenStream.getPosition();
        JavaToken characterToken = tokenStream.readCharacter();
        if (!characterToken.getValue().equals("[")) {
            return new ParseError(position, "Expected opening bracket '['");
        }

        if (characterToken.isContainsCaret()) {
            // Suggest all fields and methods of this' class, preferring integers (for index)
            List<CompletionSuggestion> suggestions = new ArrayList<>();

            List<Field> fields = parserSettings.getInspectionDataProvider().getFields(thisContextClass, false);
            suggestions.addAll(ParseUtils.createSuggestions(fields,
                                                                    ParseUtils.fieldTextInsertionInfoFunction(tokenStream.getPosition(), tokenStream.getPosition()),
                                                                    ParseUtils.FIELD_DISPLAY_FUNC,
                                                                    ParseUtils.rateFieldByClassFunc(expectedResultClass)));

            List<Method> methods = parserSettings.getInspectionDataProvider().getMethods(thisContextClass, false);
            suggestions.addAll(ParseUtils.createSuggestions(methods,
                                                                    ParseUtils.methodTextInsertionInfoFunction(tokenStream.getPosition(), tokenStream.getPosition()),
                                                                    ParseUtils.METHOD_DISPLAY_FUNC,
                                                                    ParseUtils.rateMethodByClassFunc(expectedResultClass)));

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
