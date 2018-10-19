package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class JavaFieldParser extends AbstractJavaEntityParser
{
    private final boolean staticFieldsOnly;

    JavaFieldParser(JavaInspectionDataProvider inspectionDataProvider, boolean staticFieldsOnly) {
        super(inspectionDataProvider);
        this.staticFieldsOnly = staticFieldsOnly;
    }

    @Override
    List<CompletionSuggestionIF> doParse(JavaTokenStream tokenStream, Class<?> thisContextClass) {
        try {
            JavaToken fieldNameToken = tokenStream.readIdentifier();
            if (fieldNameToken.isContainsCarret()) {
                String expectedFieldName = fieldNameToken.getToken();
                List<CompletionSuggestionIF> suggestions = new ArrayList<>();
                List<Field> fields = inspectionDataProvider.getFields(thisContextClass, staticFieldsOnly);
                for (Field field : fields) {
                    String fieldName = field.getName();
                    int matchRating = MatchValuer.rateStringMatch(fieldName, expectedFieldName);
                    CompletionSuggestionIF suggestion = new CompletionSuggestion(fieldName, matchRating);
                    suggestions.add(suggestion);
                }
                return suggestions;
            }
            // TODO: More parsing
            throw new IllegalStateException("Not yet implemented");
        } catch (JavaTokenStream.JavaTokenParseException e) {
            // TODO: Report error
            return Collections.emptyList();
        }
    }
}
