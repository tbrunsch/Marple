package com.AMS.jBEAM.javaParser;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractJavaEntityParser
{
    protected final JavaParserSettings  parserSettings;
    protected final Class<?>            thisContextClass;

    AbstractJavaEntityParser(JavaParserSettings parserSettings, Class<?> thisContextClass) {
        this.parserSettings = parserSettings;
        this.thisContextClass = thisContextClass;
    }

    abstract List<CompletionSuggestionIF> doParse(JavaTokenStream tokenStream, Class<?> currentContextClass);

    List<CompletionSuggestionIF> parse(final JavaTokenStream tokenStream, final Class<?> currentContextClass) {
        return doParse(tokenStream.clone(), currentContextClass);
    }

}
