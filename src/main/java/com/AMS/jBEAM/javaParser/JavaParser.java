package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class JavaParser
{

    public List<CompletionSuggestionIF> suggestCodeCompletion(String javaExpression, int carret, Object thisContext) {
        if (thisContext == null) {
            return Collections.emptyList();
        }
        Class<?> thisContextClass = thisContext.getClass();
        JavaParserSettings parserSettings  = new JavaParserSettings(thisContextClass);
        JavaTokenStream tokenStream = new JavaTokenStream(javaExpression, carret);
        List<CompletionSuggestionIF> suggestions = parse(tokenStream, thisContextClass,
            parserSettings.getFieldParser(false)//,
            // TODO: Add more parsers
            // parserSettings.getMethodParser(),
            // parserSettings.getClassParser(),
            // parserSettings.getConstructorParser(),
            // parserSettings.getLiteralParser(),
        );
        Collections.sort(suggestions, (s1, s2) -> Integer.compare(s1.getRating(), s2.getRating()));
        return suggestions;
    }

    static List<CompletionSuggestionIF> parse(final JavaTokenStream tokenStream, final Class<?> currentContextClass, AbstractJavaEntityParser... availableParsers) {
        List<CompletionSuggestionIF> suggestions = new ArrayList<>();
        for (AbstractJavaEntityParser parser : availableParsers) {
            suggestions.addAll(parser.parse(tokenStream, currentContextClass));
        }
        return suggestions;
    }

    static <T> List<CompletionSuggestionIF> createSuggestions(List<T> objects, ToIntFunction<T> objectRatingFunc, Function<T, String> stringRepresentationFunc) {
        return objects.stream()
            .map(object -> new CompletionSuggestion(stringRepresentationFunc.apply(object),
                                                    objectRatingFunc.applyAsInt(object)))
            .collect(Collectors.toList());
    }
}
