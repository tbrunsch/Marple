package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.tokenizer.UnaryOperator;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.OperatorResultProvider;
import com.AMS.jBEAM.javaParser.utils.OperatorResultProvider.OperatorException;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public class UnaryPrefixOperatorParser extends AbstractEntityParser
{
	private static final Map<UnaryOperator, OperatorImplementationIF>	OPERATOR_IMPLEMENTATIONS = ImmutableMap.<UnaryOperator, OperatorImplementationIF>builder()
		.put(UnaryOperator.INCREMENT, 	(infoProvider, objectInfo) -> infoProvider.getIncrementInfo	(objectInfo))
		.put(UnaryOperator.DECREMENT, 	(infoProvider, objectInfo) -> infoProvider.getDecrementInfo	(objectInfo))
		.put(UnaryOperator.PLUS, 		(infoProvider, objectInfo) -> infoProvider.getPlusInfo		(objectInfo))
		.put(UnaryOperator.MINUS, 		(infoProvider, objectInfo) -> infoProvider.getMinusInfo		(objectInfo))
		.put(UnaryOperator.LOGICAL_NOT,	(infoProvider, objectInfo) -> infoProvider.getLogicalNotInfo(objectInfo))
		.put(UnaryOperator.BITWISE_NOT,	(infoProvider, objectInfo) -> infoProvider.getBitwiseNotInfo(objectInfo))
		.build();

	public UnaryPrefixOperatorParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes) {
		Token operatorToken = tokenStream.readUnaryOperatorUnchecked();
		if (operatorToken == null) {
			log(LogLevel.ERROR, "expected unary operator");
			return new ParseError(tokenStream.getPosition(), "Expression does not start with an unary operator", ErrorType.WRONG_PARSER);
		} else if (operatorToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available");
			return CompletionSuggestions.NONE;
		}
		UnaryOperator operator = UnaryOperator.getValue(operatorToken.getValue());

		ParseResultIF parseResult = parserContext.getExpressionParser().parse(tokenStream, currentContextInfo, expectedResultTypes);

		// propagate anything except results
		if (parseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return parseResult;
		}

		ParseResult expressionParseResult = (ParseResult) parseResult;
		ObjectInfo expressionInfo = expressionParseResult.getObjectInfo();
		int parsedToPosition = expressionParseResult.getParsedToPosition();

		ObjectInfo operatorResult;
		try {
			operatorResult = applyOperator(expressionInfo, operator);
			log(LogLevel.SUCCESS, "applied operator successfully");
		} catch (OperatorException e) {
			log(LogLevel.ERROR, "applying operator failed: " + e.getMessage());
			return new ParseError(parsedToPosition, e.getMessage(), ErrorType.SEMANTIC_ERROR);
		}
		return new ParseResult(parsedToPosition, operatorResult);
	}

	private ObjectInfo applyOperator(ObjectInfo objectInfo, UnaryOperator operator) throws OperatorException {
		return OPERATOR_IMPLEMENTATIONS.get(operator).apply(parserContext.getOperatorResultProvider(), objectInfo);
	}

	@FunctionalInterface
	private interface OperatorImplementationIF
	{
		ObjectInfo apply(OperatorResultProvider operatorResultProvider, ObjectInfo objectInfo) throws OperatorException;
	}
}
