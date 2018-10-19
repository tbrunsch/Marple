package com.AMS.jBEAM.javaParser;

import java.util.List;

abstract class AbstractJavaEntityParser
{
    protected final JavaInspectionDataProvider inspectionDataProvider;

    AbstractJavaEntityParser(JavaInspectionDataProvider inspectionDataProvider) {
        this.inspectionDataProvider = inspectionDataProvider;
    }

    abstract List<CompletionSuggestionIF> doParse(JavaTokenStream tokenStream, Class<?> thisContextClass);

    List<CompletionSuggestionIF> parse(final JavaTokenStream tokenStream, final Class<?> thisContextClass) {
        return doParse(tokenStream.clone(), thisContextClass);
    }
}
