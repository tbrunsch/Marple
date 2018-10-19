package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

class JavaDotParser extends AbstractJavaEntityParser
{
    private final boolean staticOnly;

    JavaDotParser(JavaParserSettings parserSettings, Class<?> thisContextClass, boolean staticOnly) {
        super(parserSettings, thisContextClass);
        this.staticOnly = staticOnly;
    }

    @Override
    List<CompletionSuggestionIF> doParse(JavaTokenStream tokenStream, Class<?> currentContextClass) {
        try {
            JavaToken dotToken = tokenStream.readDot();
            if (dotToken.isContainsCarret()) {
                List<CompletionSuggestionIF> suggestions = new ArrayList<>();
                List<Field> fields = parserSettings.getInspectionDataProvider().getFields(currentContextClass, staticOnly);
                ToIntFunction<Field> fieldRatingFunc = field -> MatchValuer.rateStringMatch(field.getName(), "");
                suggestions.addAll(JavaParser.createSuggestions(fields, fieldRatingFunc, Field::getName));
                List<Method> methods = parserSettings.getInspectionDataProvider().getMethods(currentContextClass, staticOnly);
                ToIntFunction<Method> methodRatingFunc = method -> MatchValuer.rateStringMatch(method.getName(), "");
                suggestions.addAll(JavaParser.createSuggestions(methods, methodRatingFunc, Method::getName));
                return suggestions;
            }
            return JavaParser.parse(tokenStream, currentContextClass,
                parserSettings.getFieldParser(staticOnly)//,
                // TODO: Add more parsers
                //parserSettings.getMethodParser(staticOnly)
            );
        } catch (JavaTokenStream.JavaTokenParseException e) {
            return Collections.emptyList();
        }
    }
}
