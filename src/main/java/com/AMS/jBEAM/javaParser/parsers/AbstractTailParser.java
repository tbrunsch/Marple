package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;

abstract class AbstractTailParser<C> extends AbstractEntityParser<C>
{
	AbstractTailParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	abstract ParseResultIF parseDot(TokenStream tokenStream, C context, ParseExpectation expectation);
	abstract ParseResultIF parseOpeningSquareBracket(TokenStream tokenStream, C context, ParseExpectation expectation);
	abstract ParseResultIF createParseResult(int position, C context);

	@Override
	ParseResultIF doParse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		if (tokenStream.hasMore()) {
			char nextChar = tokenStream.peekCharacter();
			if (nextChar == '.') {
				log(LogLevel.INFO, "detected '.'");
				return parseDot(tokenStream, context, expectation);
			} else if (nextChar == '[') {
				log(LogLevel.INFO, "detected '['");
				return parseOpeningSquareBracket(tokenStream, context, expectation);
			}
		}

		int position = tokenStream.getPosition();
		return createParseResult(position, context);
	}
}
