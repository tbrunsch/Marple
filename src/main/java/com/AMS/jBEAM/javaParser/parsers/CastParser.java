package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.util.List;

public class CastParser extends AbstractEntityParser
{
	public CastParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes) {
		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			log(LogLevel.ERROR, "expected '('");
			return new ParseError(position, "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}
		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "potential cast operator; no completion suggestions available");
			return CompletionSuggestions.NONE;
		}

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ParseResultIF classParseResult = parserContext.getClassDataProvider().readClass(tokenStream.clone(), expectedResultTypes, false);
		log(LogLevel.INFO, "parse result: " + classParseResult.getResultType());

		// propagate anything except results
		if (classParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return classParseResult;
		}

		ParseResult parseResult = (ParseResult) classParseResult;
		int parsedToPosition = parseResult.getParsedToPosition();
		ObjectInfo classInfo = parseResult.getObjectInfo();
		TypeToken<?> targetType = classInfo.getDeclaredType();

		tokenStream.moveTo(parsedToPosition);

		characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
			log(LogLevel.ERROR, "missing ')' at " + tokenStream);
			return new ParseError(position, "Expected closing parenthesis ')'", ErrorType.SYNTAX_ERROR);
		}
		log(LogLevel.SUCCESS, "detected cast operator at " + tokenStream);

		if (characterToken.isContainsCaret()) {
			// nothing we can suggest after ')'
			log(LogLevel.INFO, "no completion suggestions available for position " + tokenStream);
			return CompletionSuggestions.NONE;
		}

		return parseAndCast(tokenStream, targetType);
	}

	private ParseResultIF parseAndCast(TokenStream tokenStream, TypeToken<?> targetType) {
		log(LogLevel.INFO, "parsing object to cast at " + tokenStream);
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
			ObjectInfo castInfo = parserContext.getObjectInfoProvider().getCastInfo(objectInfo, targetType);
			log(LogLevel.SUCCESS, "successfully casted object");
			return new ParseResult(parsedToPosition, castInfo);
		} catch (ClassCastException e) {
			log(LogLevel.ERROR, "class cast exception: " + e.getMessage());
			return new ParseError(tokenStream.getPosition(), "Cannot cast expression to '" + targetType + "'", ErrorType.SEMANTIC_ERROR, e);
		}
	}
}
