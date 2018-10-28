package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a sub expression starting with a dot, assuming the context
 * <ul>
 *     <li>{@code <object>.<field or method> or</li>
 *     <li>{@code <class>.<static field or method></li>
 * </ul>
 */
class JavaDotParser extends AbstractJavaEntityParser
{
    private final boolean staticOnly;

    JavaDotParser(JavaParserSettings parserSettings, Class<?> thisContextClass, boolean staticOnly) {
        super(parserSettings, thisContextClass);
        this.staticOnly = staticOnly;
    }

    @Override
    ParseResultIF doParse(JavaTokenStream tokenStream, Class<?> currentContextClass, Class<?> expectedResultClass) {
        int position = tokenStream.getPosition();
        JavaToken characterToken = tokenStream.readCharacter();
        if (!characterToken.getValue().equals(".")) {
            return new ParseError(position, "Expected dot '.'");
        }

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

            List<Field> fields = parserSettings.getInspectionDataProvider().getFields(currentContextClass, staticOnly);
            suggestions.addAll(ParseUtils.createSuggestions(fields,
                                                                    ParseUtils.fieldTextInsertionInfoFunction(insertionBegin, insertionEnd),
                                                                    ParseUtils.FIELD_DISPLAY_FUNC,
                                                                    ParseUtils.rateFieldByNameAndClassFunc("", expectedResultClass)));

            List<Method> methods = parserSettings.getInspectionDataProvider().getMethods(currentContextClass, staticOnly);
            suggestions.addAll(ParseUtils.createSuggestions(methods,
                                                                    ParseUtils.methodTextInsertionInfoFunction(insertionBegin, insertionEnd),
                                                                    ParseUtils.METHOD_DISPLAY_FUNC,
                                                                    ParseUtils.rateMethodByNameAndClassFunc("", expectedResultClass)));
            return new CompletionSuggestions(suggestions);
        }

        return JavaParser.parse(tokenStream, currentContextClass, expectedResultClass,
            parserSettings.getFieldParser(staticOnly)//,
            // TODO: Add method parser
            //parserSettings.getMethodParser(staticOnly)
        );
    }
}
