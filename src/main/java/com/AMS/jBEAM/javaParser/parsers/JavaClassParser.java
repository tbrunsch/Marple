package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.JavaParserContext;
import com.AMS.jBEAM.javaParser.result.ParseError;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.result.ParseResult;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.result.ParseResultType;
import com.AMS.jBEAM.javaParser.tokenizer.JavaToken;
import com.AMS.jBEAM.javaParser.tokenizer.JavaTokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;

import java.util.List;

public class JavaClassParser extends AbstractJavaEntityParser
{
	public JavaClassParser(JavaParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		ParseResultIF classParseResult = parserContext.getClassDataProvider().readClass(tokenStream, expectedResultClasses, false, true);

		// propagate anything except results
		if (classParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return classParseResult;
		}

		ParseResult parseResult = (ParseResult) classParseResult;
		int parsedToPosition = parseResult.getParsedToPosition();
		ObjectInfo classInfo = parseResult.getObjectInfo();

		tokenStream.moveTo(parsedToPosition);

		if (!tokenStream.hasMore() || tokenStream.peekCharacter() != '.') {
			return new ParseError(tokenStream.getPosition(), "Expected a dot '.'", ErrorType.WRONG_PARSER);
		}
		return parserContext.getTailParser(true).parse(tokenStream, classInfo, expectedResultClasses);
	}
}
