package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.util.List;

public class ClassParser extends AbstractEntityParser
{
	public ClassParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes) {
		log(LogLevel.INFO, "parsing class");
		ParseResultIF classParseResult = parserContext.getClassDataProvider().readClass(tokenStream, true);
		log(LogLevel.INFO, "parse result: " + classParseResult.getResultType());

		// propagate anything except results
		if (classParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return classParseResult;
		}

		ParseResult parseResult = (ParseResult) classParseResult;
		int parsedToPosition = parseResult.getParsedToPosition();
		ObjectInfo classInfo = parseResult.getObjectInfo();

		tokenStream.moveTo(parsedToPosition);

		if (!tokenStream.hasMore() || tokenStream.peekCharacter() != '.') {
			log(LogLevel.ERROR, "missing '.' at " + tokenStream);
			return new ParseError(tokenStream.getPosition(), "Expected a dot '.'", ErrorType.WRONG_PARSER);
		}
		return parserContext.getTailParser(true).parse(tokenStream, classInfo, expectedResultTypes);
	}
}
