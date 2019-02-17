package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.ClassParseResult;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

public class InnerClassParser extends AbstractEntityParser<TypeToken<?>>
{
	public InnerClassParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, TypeToken<?> contextType, ParseExpectation expectation) {
		ParseResultIF innerClassParseResult = parserContext.getClassDataProvider().readInnerClass(tokenStream, contextType);

		if (ParseUtils.propagateParseResult(innerClassParseResult, ParseExpectation.CLASS)) {
			return innerClassParseResult;
		}

		ClassParseResult parseResult = (ClassParseResult) innerClassParseResult;
		int parsedToPosition = parseResult.getPosition();
		TypeToken<?> innerClassType = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		return parserContext.getClassTailParser().parse(tokenStream, innerClassType, expectation);
	}
}
