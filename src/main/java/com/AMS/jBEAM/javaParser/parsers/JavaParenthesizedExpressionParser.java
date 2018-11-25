package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.JavaParserContext;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.result.ParseError;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.result.ParseResult;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.JavaToken;
import com.AMS.jBEAM.javaParser.tokenizer.JavaTokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;

import java.util.List;

public class JavaParenthesizedExpressionParser extends AbstractJavaEntityParser
{
	public JavaParenthesizedExpressionParser(JavaParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		int position = tokenStream.getPosition();
		JavaToken characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			return new ParseError(position, "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}
		if (characterToken.isContainsCaret()) {
			return parserContext.getFieldAndMethodDataProvider().suggestFieldsAndMethods(tokenStream, expectedResultClasses);
		}

		ParseResultIF expressionParseResult = parserContext.getExpressionParser().parse(tokenStream, currentContextInfo, expectedResultClasses);
		switch (expressionParseResult.getResultType()) {
			case COMPLETION_SUGGESTIONS:
				// code completion inside "()" => propagate completion suggestions
				return expressionParseResult;
			case PARSE_ERROR:
			case AMBIGUOUS_PARSE_RESULT:
				// always propagate errors
				return expressionParseResult;
			case PARSE_RESULT: {
				ParseResult parseResult = (ParseResult) expressionParseResult;
				int parsedToPosition = parseResult.getParsedToPosition();
				ObjectInfo objectInfo = parseResult.getObjectInfo();
				tokenStream.moveTo(parsedToPosition);

				characterToken = tokenStream.readCharacterUnchecked();
				if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
					return new ParseError(position, "Expected closing parenthesis ')'", ErrorType.SYNTAX_ERROR);
				}
				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after ')'
					return CompletionSuggestions.NONE;
				}

				return parserContext.getObjectTailParser().parse(tokenStream, objectInfo, expectedResultClasses);
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + expressionParseResult.getResultType());
		}
	}
}
