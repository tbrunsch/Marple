package com.AMS.jBEAM.javaParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaParser
{
    private final JavaInspectionDataProvider    inspectionDataProvider  = new JavaInspectionDataProvider();

    private final JavaFieldParser               fieldParser             = new JavaFieldParser(inspectionDataProvider, false);

    public List<CompletionSuggestionIF> suggestCodeCompletion(String javaExpression, int carret, Object thisContext) {
        if (thisContext == null) {
            return Collections.emptyList();
        }
        Class<?> thisContextClass = thisContext.getClass();
        JavaTokenStream tokenStream = new JavaTokenStream(javaExpression, carret);
        List<CompletionSuggestionIF> suggestions = new ArrayList<>();
        suggestions.addAll(fieldParser.parse(tokenStream, thisContextClass));
        // TODO: Handle more cases

        Collections.sort(suggestions, (s1, s2) -> Integer.compare(s1.getRating(), s2.getRating()));
        return suggestions;
    }
}
