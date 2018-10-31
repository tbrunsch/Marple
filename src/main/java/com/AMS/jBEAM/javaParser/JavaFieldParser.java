package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * Parses a sub expression starting with a field {@code <field>}, assuming the context
 * <ul>
 *     <li>{@code <context instance>.<field>},</li>
 *     <li>{@code <context class>.<field>}, or</li>
 *     <li>{@code <field>} (like {@code <context instance>.<field>} for {@code <context instance> = this})</li>
 * </ul>
 */
class JavaFieldParser extends AbstractJavaEntityParser
{
    private final boolean staticOnly;

    JavaFieldParser(JavaParserSettings parserSettings, Class<?> thisContextClass, boolean staticOnly) {
        super(parserSettings, thisContextClass);
        this.staticOnly = staticOnly;
    }

    @Override
    ParseResultIF doParse(JavaTokenStream tokenStream, Class<?> currentContextClass, Class<?> expectedResultClass) {
        int startPosition = tokenStream.getPosition();
        JavaToken fieldNameToken;
        try {
            fieldNameToken = tokenStream.readIdentifier();
        } catch (JavaTokenStream.JavaTokenParseException e) {
            return new ParseError(startPosition, "Expected an identifier");
        }
        String fieldName = fieldNameToken.getValue();
        int endPosition = tokenStream.getPosition();

        List<Field> fields = parserSettings.getInspectionDataProvider().getFields(currentContextClass, staticOnly);

        // check for code completion
        if (fieldNameToken.isContainsCaret()) {
            List<CompletionSuggestion> suggestions = ParseUtils.createSuggestions(
				fields,
				ParseUtils.fieldTextInsertionInfoFunction(startPosition, endPosition),
				ParseUtils.FIELD_DISPLAY_FUNC,
				ParseUtils.rateFieldByNameAndClassFunc(fieldName, expectedResultClass)
			);
            return new CompletionSuggestions(suggestions);
        }

        // no code completion requested => field name must exist
        Optional<Field> firstFieldMatch = fields.stream().filter(field -> field.getName().equals(fieldName)).findFirst();
        if (!firstFieldMatch.isPresent()) {
            return new ParseError(startPosition, "Unknown field '" + fieldName + "'");
        }

        Field matchingField = firstFieldMatch.get();
        Class<?> matchingFieldClass = matchingField.getType();

        return parserSettings.getObjectTailParser().parse(tokenStream, matchingFieldClass, expectedResultClass);
    }
}
