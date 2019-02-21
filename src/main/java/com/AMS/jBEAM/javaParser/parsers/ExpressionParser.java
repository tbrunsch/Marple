package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;

/**
 * Parses an arbitrary Java expression without binary operators
 */
public class ExpressionParser extends AbstractEntityParser<ObjectInfo>
{
	public ExpressionParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		return ParseUtils.parse(tokenStream, contextInfo, expectation,
			parserToolbox.getLiteralParser(),
			parserToolbox.getObjectFieldParser(),
			parserToolbox.getObjectMethodParser(),
			parserToolbox.getParenthesizedExpressionParser(),
			parserToolbox.getCastParser(),
			parserToolbox.getClassParser(),
			parserToolbox.getConstructorParser(),
			parserToolbox.getUnaryPrefixOperatorParser(),
			parserToolbox.getVariableParser(),
			parserToolbox.getCustomHierarchyParser()
		);
	}
}
