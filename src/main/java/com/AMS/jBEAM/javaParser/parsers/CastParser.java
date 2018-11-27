package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.JavaParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;

import java.util.List;

public class CastParser extends AbstractEntityParser
{
	public CastParser(JavaParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			return new ParseError(position, "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}
		if (characterToken.isContainsCaret()) {
			// nothing we can suggest after '('
			return CompletionSuggestions.NONE;
		}

		ParseResultIF classParseResult = parserContext.getClassDataProvider().readClass(tokenStream, expectedResultClasses, true, false);

		// propagate anything except results
		if (classParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return classParseResult;
		}

		ParseResult parseResult = (ParseResult) classParseResult;
		int parsedToPosition = parseResult.getParsedToPosition();
		ObjectInfo classInfo = parseResult.getObjectInfo();
		Class<?> targetClass = classInfo.getDeclaredClass();

		tokenStream.moveTo(parsedToPosition);

		characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
			return new ParseError(position, "Expected closing parenthesis ')'", ErrorType.SYNTAX_ERROR);
		}
		if (characterToken.isContainsCaret()) {
			// nothing we can suggest after ')'
			return CompletionSuggestions.NONE;
		}

		return parseAndCast(tokenStream, targetClass);
	}

	private ParseResultIF parseAndCast(TokenStream tokenStream, Class<?> targetClass) {
		ParseResultIF objectParseResult = parserContext.getExpressionParser().parse(tokenStream, thisInfo, null);

		// propagate anything except results
		if (objectParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return objectParseResult;
		}

		ParseResult parseResult = (ParseResult) objectParseResult;
		int parsedToPosition = parseResult.getParsedToPosition();
		ObjectInfo objectInfo = parseResult.getObjectInfo();
		tokenStream.moveTo(parsedToPosition);

		try {
			ObjectInfo castInfo = parserContext.getObjectInfoProvider().getCastInfo(objectInfo, targetClass);
			return new ParseResult(parsedToPosition, castInfo);
		} catch (ClassCastException e) {
			return new ParseError(tokenStream.getPosition(), "Cannot cast expression to '" + targetClass + "'", ErrorType.SEMANTIC_ERROR, e);
		}
	}
}
