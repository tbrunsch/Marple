package com.AMS.jBEAM.javaParser;

import java.util.List;

/**
 * Parses an arbitrary Java expression
 */
class JavaExpressionParser extends AbstractJavaEntityParser
{
	JavaExpressionParser(JavaParserPool parserSettings, ObjectInfo thisInfo) {
		super(parserSettings, thisInfo);
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		return JavaParser.parse(tokenStream, thisInfo, expectedResultClasses,
			parserPool.getLiteralParser(),
			parserPool.getFieldParser(false),
			parserPool.getMethodParser(false),
			parserPool.getParenthesizedExpressionParser(),
			parserPool.getCastParser()
			// TODO: Add more parsers
			// parserPool.getClassParser(),
			// parserPool.getConstructorParser()
		);
	}
}
