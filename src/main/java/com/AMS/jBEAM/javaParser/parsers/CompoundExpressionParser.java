package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.EvaluationMode;
import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.tokenizer.BinaryOperator;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.BinaryOperatorResultProvider;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.AMS.jBEAM.javaParser.utils.BinaryOperatorResultProvider.*;

public class CompoundExpressionParser extends AbstractEntityParser
{
	private static final Map<BinaryOperator, OperatorImplementationIF>	OPERATOR_IMPLEMENTATIONS = ImmutableMap.<BinaryOperator, OperatorImplementationIF>builder()
		.put(BinaryOperator.MULTIPLY, 					(infoProvider, lhs, rhs) -> infoProvider.getMultiplicationInfo		(lhs, rhs))
		.put(BinaryOperator.DIVIDE, 					(infoProvider, lhs, rhs) -> infoProvider.getDivisionInfo			(lhs, rhs))
		.put(BinaryOperator.MODULO,						(infoProvider, lhs, rhs) -> infoProvider.getModuloInfo				(lhs, rhs))
		.put(BinaryOperator.ADD_OR_CONCAT, 				(infoProvider, lhs, rhs) -> infoProvider.getAddOrConcatInfo			(lhs, rhs))
		.put(BinaryOperator.SUBTRACT, 					(infoProvider, lhs, rhs) -> infoProvider.getSubtractionInfo			(lhs, rhs))
		.put(BinaryOperator.LEFT_SHIFT,					(infoProvider, lhs, rhs) -> infoProvider.getLeftShiftInfo			(lhs, rhs))
		.put(BinaryOperator.RIGHT_SHIFT, 				(infoProvider, lhs, rhs) -> infoProvider.getRightShiftInfo			(lhs, rhs))
		.put(BinaryOperator.UNSIGNED_RIGHT_SHIFT, 		(infoProvider, lhs, rhs) -> infoProvider.getUnsignedRightShiftInfo	(lhs, rhs))
		.put(BinaryOperator.LESS_THAN, 					(infoProvider, lhs, rhs) -> infoProvider.getLessThanInfo			(lhs, rhs))
		.put(BinaryOperator.LESS_THAN_OR_EQUAL_TO,		(infoProvider, lhs, rhs) -> infoProvider.getLessThanOrEqualToInfo	(lhs, rhs))
		.put(BinaryOperator.GREATER_THAN, 				(infoProvider, lhs, rhs) -> infoProvider.getGreaterThanInfo			(lhs, rhs))
		.put(BinaryOperator.GREATER_THAN_OR_EQUAL_TO,	(infoProvider, lhs, rhs) -> infoProvider.getGreaterThanOrEqualToInfo(lhs, rhs))
		.put(BinaryOperator.EQUAL_TO, 					(infoProvider, lhs, rhs) -> infoProvider.getEqualToInfo				(lhs, rhs))
		.put(BinaryOperator.NOT_EQUAL_TO, 				(infoProvider, lhs, rhs) -> infoProvider.getNotEqualToInfo			(lhs, rhs))
		.put(BinaryOperator.BITWISE_AND, 				(infoProvider, lhs, rhs) -> infoProvider.getBitwiseAndInfo			(lhs, rhs))
		.put(BinaryOperator.BITWISE_XOR, 				(infoProvider, lhs, rhs) -> infoProvider.getBitwiseXorInfo			(lhs, rhs))
		.put(BinaryOperator.BITWISE_OR, 				(infoProvider, lhs, rhs) -> infoProvider.getBitwiseOrInfo			(lhs, rhs))
		.put(BinaryOperator.LOGICAL_AND, 				(infoProvider, lhs, rhs) -> infoProvider.getLogicalAndInfo			(lhs, rhs))
		.put(BinaryOperator.LOGICAL_OR, 				(infoProvider, lhs, rhs) -> infoProvider.getLogicalOrInfo			(lhs, rhs))
		.put(BinaryOperator.ASSIGNMENT, 				(infoProvider, lhs, rhs) -> infoProvider.getAssignmentInfo			(lhs, rhs))
		.build();

	private final int maxOperatorPrecedenceLevelToConsider;

	public CompoundExpressionParser(ParserContext parserContext, ObjectInfo thisInfo, int maxOperatorPrecedenceLevelToConsider) {
		super(parserContext, thisInfo);
		this.maxOperatorPrecedenceLevelToConsider = maxOperatorPrecedenceLevelToConsider;
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		ParseResultIF parseResult = parserContext.getExpressionParser().parse(tokenStream, currentContextInfo, expectedResultClasses);

		// propagate anything except results
		if (parseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return parseResult;
		}

		ParseResult lhsParseResult = (ParseResult) parseResult;
		ObjectInfo lhsInfo = lhsParseResult.getObjectInfo();
		int parsedToPosition = lhsParseResult.getParsedToPosition();
		tokenStream.moveTo(parsedToPosition);

		/*
		 * If short circuit evaluation becomes active, then we must switch to a non-evaluating context,
		 * but still check for syntax errors.
		 */
		ParserContext context = parserContext;
		boolean considerOperatorResult = true;
		while (true) {
			Token operatorToken = tokenStream.readBinaryOperatorUnchecked();
			if (operatorToken == null) {
				return ParseUtils.createParseResult(context, lhsInfo, expectedResultClasses, parsedToPosition);
			}
			if (operatorToken.isContainsCaret()) {
				// No suggestions possible
				return CompletionSuggestions.NONE;
			}

			BinaryOperator operator = BinaryOperator.getValue(operatorToken.getValue());
			if (operator.getPrecedenceLevel() > maxOperatorPrecedenceLevelToConsider) {
				return ParseUtils.createParseResult(context, lhsInfo, expectedResultClasses, parsedToPosition);
			}

			switch (operator.getAssociativity()) {
				case LEFT_TO_RIGHT: {
					if (considerOperatorResult && stopCircuitEvaluation(lhsInfo, operator)) {
						context = createContextWithoutEvaluation();
						considerOperatorResult = false;
					}
					parseResult = context.createCompoundExpressionParser(operator.getPrecedenceLevel() - 1).parse(tokenStream, currentContextInfo, expectedResultClasses);

					// propagate anything except results
					if (parseResult.getResultType() != ParseResultType.PARSE_RESULT) {
						return parseResult;
					}
					ParseResult rhsParseResult = (ParseResult) parseResult;
					ObjectInfo rhsInfo = rhsParseResult.getObjectInfo();
					parsedToPosition = rhsParseResult.getParsedToPosition();
					tokenStream.moveTo(parsedToPosition);

					try {
						// Check syntax even if result of operator is not considered because of short circuit evaluation
						ObjectInfo operatorResult = applyOperator(context, lhsInfo, rhsInfo, operator);
						if (considerOperatorResult) {
							lhsInfo = operatorResult;
						}
					} catch (OperatorException e) {
						return new ParseError(parsedToPosition, e.getMessage(), ErrorType.SEMANTIC_ERROR);
					}
					break;
				}
				case RIGHT_TO_LEFT: {
					parseResult = context.createCompoundExpressionParser(operator.getPrecedenceLevel()).parse(tokenStream, currentContextInfo, expectedResultClasses);

					// propagate anything except results
					if (parseResult.getResultType() != ParseResultType.PARSE_RESULT) {
						return parseResult;
					}
					ParseResult rhsParseResult = (ParseResult) parseResult;
					ObjectInfo rhsInfo = rhsParseResult.getObjectInfo();

					ObjectInfo operatorResultInfo;
					try {
						operatorResultInfo = applyOperator(context, lhsInfo, rhsInfo, operator);
					} catch (OperatorException e) {
						return new ParseError(rhsParseResult.getParsedToPosition(), e.getMessage(), ErrorType.SEMANTIC_ERROR);
					}
					return ParseUtils.createParseResult(context, operatorResultInfo, expectedResultClasses, rhsParseResult.getParsedToPosition());
				}
				default:
					return new ParseError(tokenStream.getPosition(), "Internal error: Unknown operator associativity: " + operator.getAssociativity(), ErrorType.INTERNAL_ERROR);
			}
		}
	}

	private ObjectInfo applyOperator(ParserContext context, ObjectInfo lhs, ObjectInfo rhs, BinaryOperator operator) throws OperatorException {
		return OPERATOR_IMPLEMENTATIONS.get(operator).apply(context.getBinaryOperatorResultProvider(), lhs, rhs);
	}

	private boolean stopCircuitEvaluation(ObjectInfo objectInfo, BinaryOperator operator) {
		return operator == BinaryOperator.LOGICAL_AND	&& Boolean.FALSE.equals(objectInfo.getObject())
			|| operator == BinaryOperator.LOGICAL_OR	&& Boolean.TRUE.equals(objectInfo.getObject());
	}

	private ParserContext createContextWithoutEvaluation() {
		return new ParserContext(parserContext.getThisInfo(), parserContext.getSettings(), EvaluationMode.NONE);
	}

	@FunctionalInterface
	private interface OperatorImplementationIF
	{
		ObjectInfo apply(BinaryOperatorResultProvider binaryOperatorResultProvider, ObjectInfo lhs, ObjectInfo rhs) throws OperatorException;
	}
}
