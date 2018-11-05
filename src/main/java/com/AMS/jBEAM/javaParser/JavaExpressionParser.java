package com.AMS.jBEAM.javaParser;

/**
 * Parses an arbitrary Java expression
 */
class JavaExpressionParser extends AbstractJavaEntityParser
{
    JavaExpressionParser(JavaParserPool parserSettings, ObjectInfo thisInfo) {
        super(parserSettings, thisInfo);
    }

    @Override
    ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, Class<?> expectedResultClass) {
        return JavaParser.parse(tokenStream, thisInfo, expectedResultClass,
            parserPool.getFieldParser(false),
            parserPool.getMethodParser(false)//,
			// TODO: Add more parsers
            // parserPool.getClassParser(),
            // parserPool.getConstructorParser(),
			// parserPool.getCastParser(),
            // parserPool.getLiteralParser(), // also parses "null"
        );
    }
}
