package com.AMS.jBEAM.javaParser;

import java.util.List;

/**
 * Parses an arbitrary Java expression
 */
class JavaExpressionParser extends AbstractJavaEntityParser
{
    JavaExpressionParser(JavaParserSettings parserSettings, Class<?> thisContextClass) {
        super(parserSettings, thisContextClass);
    }

    @Override
    ParseResultIF doParse(JavaTokenStream tokenStream, Class<?> currentContextClass) {
        return JavaParser.parse(tokenStream, thisContextClass,
            parserSettings.getFieldParser(false)//,
            // TODO: Add more parsers
            // parserSettings.getMethodParser(),
            // parserSettings.getClassParser(),
            // parserSettings.getConstructorParser(),
            // parserSettings.getLiteralParser(),
        );
    }
}
