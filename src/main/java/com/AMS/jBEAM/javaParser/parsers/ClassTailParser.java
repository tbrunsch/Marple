package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.ClassParseResult;
import com.AMS.jBEAM.javaParser.result.ParseError;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

/**
 * Parses a sub expression following a complete Java expression, assuming the context {@code <class>
 */
public class ClassTailParser extends AbstractTailParser<TypeToken<?>>
{
	public ClassTailParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF parseDot(TokenStream tokenStream, TypeToken<?> classType, ParseExpectation expectation) {
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals(".");

		AbstractEntityParser<TypeToken<?>> fieldParser = parserContext.getClassFieldParser();
		AbstractEntityParser<TypeToken<?>> methodParser = parserContext.getClassMethodParser();
		AbstractEntityParser<TypeToken<?>> innerClassParser = parserContext.getInnerClassParser();
		return ParseUtils.parse(tokenStream, classType, expectation,
			fieldParser,
			methodParser,
			innerClassParser
		);
	}

	@Override
	ParseResultIF parseOpeningSquareBracket(TokenStream tokenStream, TypeToken<?> context, ParseExpectation expectation) {
		/*
		 * If called under ConstructorParser, then this is an array construction. As we do not
		 * know, in which circumstances this method is called, the caller must handle this
		 * operator. Hence, we stop parsing here.
		 */
		return new ClassParseResult(tokenStream.getPosition(), context);
	}

	@Override
	ParseResultIF createParseResult(int position, TypeToken<?> type) {
		return new ClassParseResult(position, type);
	}
}
