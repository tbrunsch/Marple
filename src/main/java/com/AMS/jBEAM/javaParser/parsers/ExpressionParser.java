package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;

/**
 * Parses an arbitrary Java expression without binary operators
 */
public class ExpressionParser extends AbstractEntityParser<ObjectInfo>
{
	public ExpressionParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		return ParseUtils.parse(tokenStream, contextInfo, expectation,
			parserContext.getLiteralParser(),
			parserContext.getObjectFieldParser(),
			parserContext.getObjectMethodParser(),
			parserContext.getParenthesizedExpressionParser(),
			parserContext.getCastParser(),
			parserContext.getTopLevelClassParser(),
			parserContext.getConstructorParser(),
			parserContext.getUnaryPrefixOperatorParser(),
			parserContext.getVariableParser(),
			parserContext.getCustomHierarchyParser()
		);
	}
}
