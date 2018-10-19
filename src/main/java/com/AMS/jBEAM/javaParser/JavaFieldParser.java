package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

class JavaFieldParser extends AbstractJavaEntityParser
{
    private final boolean staticOnly;

    JavaFieldParser(JavaParserSettings parserSettings, Class<?> thisContextClass, boolean staticOnly) {
        super(parserSettings, thisContextClass);
        this.staticOnly = staticOnly;
    }

    @Override
    List<CompletionSuggestionIF> doParse(JavaTokenStream tokenStream, Class<?> currentContextClass) {
        try {
            List<Field> fields = parserSettings.getInspectionDataProvider().getFields(currentContextClass, staticOnly);
            JavaToken fieldNameToken = tokenStream.readIdentifier();
            String fieldName = fieldNameToken.getToken();
            if (fieldNameToken.isContainsCarret()) {
                ToIntFunction<Field> fieldRatingFunc = field -> MatchValuer.rateStringMatch(field.getName(), fieldName);
                return JavaParser.createSuggestions(fields, fieldRatingFunc, Field::getName);
            }
            // expecting exact match
            Optional<Field> firstFieldMatch = fields.stream().filter(field -> field.getName().equals(fieldName)).findFirst();
            if (!firstFieldMatch.isPresent()) {
                return Collections.emptyList();
            }
            Field matchingField = firstFieldMatch.get();
            Class<?> matchingFieldClass = matchingField.getType();
            return JavaParser.parse(tokenStream, matchingFieldClass,
                parserSettings.getDotParser(staticOnly) //,
                // TODO: Add more parsers
                //parserSettings.getArrayAccessParser(),    // []
                //parserSettings.getComparisonParser(),     // ==, !=, <, >, <=, >=
                //parserSettings.getAssignmentParser()
            );
        } catch (JavaTokenStream.JavaTokenParseException e) {
            return Collections.emptyList();
        }
    }
}
