package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.ClassParseResult;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

public class ClassParser extends AbstractEntityParser<ObjectInfo>
{
	public ClassParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		log(LogLevel.INFO, "parsing class");
		ParseResultIF classParseResult = parserContext.getClassDataProvider().readClass(tokenStream);
		log(LogLevel.INFO, "parse result: " + classParseResult.getResultType());

		if (ParseUtils.propagateParseResult(classParseResult, ParseExpectation.CLASS)) {
			return classParseResult;
		}

		ClassParseResult parseResult = (ClassParseResult) classParseResult;
		int parsedToPosition = parseResult.getPosition();
		TypeToken<?> type = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		return parserContext.getClassTailParser().parse(tokenStream, type, expectation);
	}
}
