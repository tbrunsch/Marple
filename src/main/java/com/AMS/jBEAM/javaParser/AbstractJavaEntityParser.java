package com.AMS.jBEAM.javaParser;

abstract class AbstractJavaEntityParser
{
    protected final JavaParserSettings  parserSettings;
    protected final Class<?>            thisContextClass;

    AbstractJavaEntityParser(JavaParserSettings parserSettings, Class<?> thisContextClass) {
        this.parserSettings = parserSettings;
        this.thisContextClass = thisContextClass;
    }

    abstract ParseResultIF doParse(JavaTokenStream tokenStream, Class<?> currentContextClass);

    ParseResultIF parse(final JavaTokenStream tokenStream, final Class<?> currentContextClass) {
        return doParse(tokenStream.clone(), currentContextClass);
    }
}
