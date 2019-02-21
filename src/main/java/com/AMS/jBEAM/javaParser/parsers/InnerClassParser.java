package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.result.ClassParseResult;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

public class InnerClassParser extends AbstractEntityParser<TypeToken<?>>
{
	public InnerClassParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, TypeToken<?> contextType, ParseExpectation expectation) {
		ParseResultIF innerClassParseResult = parserToolbox.getClassDataProvider().readInnerClass(tokenStream, contextType);

		if (ParseUtils.propagateParseResult(innerClassParseResult, ParseExpectation.CLASS)) {
			return innerClassParseResult;
		}

		ClassParseResult parseResult = (ClassParseResult) innerClassParseResult;
		int parsedToPosition = parseResult.getPosition();
		TypeToken<?> innerClassType = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		return parserToolbox.getClassTailParser().parse(tokenStream, innerClassType, expectation);
	}
}
