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
    ParseResultIF doParse(JavaTokenStream tokenStream, Class<?> currentContextClass, Class<?> expectedResultClass) {
        return JavaParser.parse(tokenStream, thisContextClass, expectedResultClass,
            parserSettings.getFieldParser(false)//,
            // TODO: Add more parsers
            // parserSettings.getMethodParser(),
            // parserSettings.getClassParser(),
            // parserSettings.getConstructorParser(),
            // parserSettings.getLiteralParser(), // also parses "null"
        );
    }
}
