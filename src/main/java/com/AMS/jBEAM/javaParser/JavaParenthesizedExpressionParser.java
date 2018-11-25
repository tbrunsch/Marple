package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.ParseError.ErrorType;

import java.util.List;

class JavaParenthesizedExpressionParser extends AbstractJavaEntityParser
{
	JavaParenthesizedExpressionParser(JavaParserPool parserPool, ObjectInfo thisInfo) {
		super(parserPool, thisInfo);
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		int position = tokenStream.getPosition();
		JavaToken characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			return new ParseError(position, "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}
		if (characterToken.isContainsCaret()) {
			return suggestFieldsAndMethods(tokenStream, expectedResultClasses);
		}

		ParseResultIF expressionParseResult = parserPool.getExpressionParser().parse(tokenStream, currentContextInfo, expectedResultClasses);
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

				return parserPool.getObjectTailParser().parse(tokenStream, objectInfo, expectedResultClasses);
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + expressionParseResult.getResultType());
		}
	}
}
