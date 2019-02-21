package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.settings.ParseMode;
import com.AMS.jBEAM.javaParser.tokenizer.BinaryOperator;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.dataProviders.OperatorResultProvider;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.AMS.jBEAM.javaParser.utils.dataProviders.OperatorResultProvider.OperatorException;

/**
 * Parses arbitrary Java expressions including binary operators by using the
 * {@link ExpressionParser} for parsing the operands.
 */
public class RootParser extends AbstractEntityParser<ObjectInfo>
{
	private static final Map<BinaryOperator, OperatorImplementationIF>	OPERATOR_IMPLEMENTATIONS = ImmutableMap.<BinaryOperator, OperatorImplementationIF>builder()
		.put(BinaryOperator.MULTIPLY, 					OperatorResultProvider::getMultiplicationInfo)
		.put(BinaryOperator.DIVIDE, 					OperatorResultProvider::getDivisionInfo)
		.put(BinaryOperator.MODULO,						OperatorResultProvider::getModuloInfo)
		.put(BinaryOperator.ADD_OR_CONCAT, 				OperatorResultProvider::getAddOrConcatInfo)
		.put(BinaryOperator.SUBTRACT, 					OperatorResultProvider::getSubtractionInfo)
		.put(BinaryOperator.LEFT_SHIFT,					OperatorResultProvider::getLeftShiftInfo)
		.put(BinaryOperator.RIGHT_SHIFT, 				OperatorResultProvider::getRightShiftInfo)
		.put(BinaryOperator.UNSIGNED_RIGHT_SHIFT, 		OperatorResultProvider::getUnsignedRightShiftInfo)
		.put(BinaryOperator.LESS_THAN, 					OperatorResultProvider::getLessThanInfo)
		.put(BinaryOperator.LESS_THAN_OR_EQUAL_TO,		OperatorResultProvider::getLessThanOrEqualToInfo)
		.put(BinaryOperator.GREATER_THAN, 				OperatorResultProvider::getGreaterThanInfo)
		.put(BinaryOperator.GREATER_THAN_OR_EQUAL_TO,	OperatorResultProvider::getGreaterThanOrEqualToInfo)
		.put(BinaryOperator.EQUAL_TO, 					OperatorResultProvider::getEqualToInfo)
		.put(BinaryOperator.NOT_EQUAL_TO, 				OperatorResultProvider::getNotEqualToInfo)
		.put(BinaryOperator.BITWISE_AND, 				OperatorResultProvider::getBitwiseAndInfo)
		.put(BinaryOperator.BITWISE_XOR, 				OperatorResultProvider::getBitwiseXorInfo)
		.put(BinaryOperator.BITWISE_OR, 				OperatorResultProvider::getBitwiseOrInfo)
		.put(BinaryOperator.LOGICAL_AND, 				OperatorResultProvider::getLogicalAndInfo)
		.put(BinaryOperator.LOGICAL_OR, 				OperatorResultProvider::getLogicalOrInfo)
		.put(BinaryOperator.ASSIGNMENT, 				OperatorResultProvider::getAssignmentInfo)
		.build();

	private final int maxOperatorPrecedenceLevelToConsider;

	public RootParser(ParserToolbox parserToolbox, ObjectInfo thisInfo, int maxOperatorPrecedenceLevelToConsider) {
		super(parserToolbox, thisInfo);
		this.maxOperatorPrecedenceLevelToConsider = maxOperatorPrecedenceLevelToConsider;
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		log(LogLevel.INFO, "parsing first expression");
		ParseResultIF parseResult = parserToolbox.getExpressionParser().parse(tokenStream, contextInfo, expectation);

		if (ParseUtils.propagateParseResult(parseResult, expectation)) {
			return parseResult;
		}
		ObjectParseResult lhsParseResult = (ObjectParseResult) parseResult;
		ObjectInfo lhsInfo = lhsParseResult.getObjectInfo();
		int parsedToPosition = lhsParseResult.getPosition();
		tokenStream.moveTo(parsedToPosition);

		/*
		 * If short circuit evaluation becomes active, then we must switch to a non-evaluating contextInfo,
		 * but still check for syntax errors.
		 */

		ParserToolbox parserToolbox = this.parserToolbox;
		boolean considerOperatorResult = true;
		while (true) {
			Token operatorToken = tokenStream.readBinaryOperatorUnchecked();
			if (operatorToken == null) {
				return new ObjectParseResult(parsedToPosition, lhsInfo);
			}
			log(LogLevel.SUCCESS, "detected binary operator '" + operatorToken.getValue() + "' at " + tokenStream);

			if (operatorToken.isContainsCaret()) {
				log(LogLevel.INFO, "no completion suggestions available");
				return CompletionSuggestions.none(tokenStream.getPosition());
			}

			BinaryOperator operator = BinaryOperator.getValue(operatorToken.getValue());
			if (operator.getPrecedenceLevel() > maxOperatorPrecedenceLevelToConsider) {
				return new ObjectParseResult(parsedToPosition, lhsInfo);
			}

			switch (operator.getAssociativity()) {
				case LEFT_TO_RIGHT: {
					if (considerOperatorResult && stopCircuitEvaluation(lhsInfo, operator)) {
						parserToolbox = createToolboxWithoutEvaluation();
						considerOperatorResult = false;
					}
					parseResult = parserToolbox.createRootParser(operator.getPrecedenceLevel() - 1).parse(tokenStream, contextInfo, ParseExpectation.OBJECT);

					if (ParseUtils.propagateParseResult(parseResult, ParseExpectation.OBJECT)) {
						return parseResult;
					}
					ObjectParseResult rhsParseResult = (ObjectParseResult) parseResult;
					ObjectInfo rhsInfo = rhsParseResult.getObjectInfo();
					parsedToPosition = rhsParseResult.getPosition();
					tokenStream.moveTo(parsedToPosition);

					try {
						// Check syntax even if result of operator is not considered because of short circuit evaluation
						ObjectInfo operatorResult = applyOperator(parserToolbox, lhsInfo, rhsInfo, operator);
						if (considerOperatorResult) {
							lhsInfo = operatorResult;
						}
						log(LogLevel.SUCCESS, "applied operator successfully");
					} catch (OperatorException e) {
						log(LogLevel.ERROR, "applying operator failed: " + e.getMessage());
						return new ParseError(parsedToPosition, e.getMessage(), ErrorType.SEMANTIC_ERROR);
					}
					break;
				}
				case RIGHT_TO_LEFT: {
					parseResult = parserToolbox.createRootParser(operator.getPrecedenceLevel()).parse(tokenStream, contextInfo, ParseExpectation.OBJECT);

					if (ParseUtils.propagateParseResult(parseResult, ParseExpectation.OBJECT)) {
						return parseResult;
					}
					ObjectParseResult rhsParseResult = (ObjectParseResult) parseResult;
					ObjectInfo rhsInfo = rhsParseResult.getObjectInfo();

					ObjectInfo operatorResultInfo;
					try {
						operatorResultInfo = applyOperator(parserToolbox, lhsInfo, rhsInfo, operator);
						log(LogLevel.SUCCESS, "applied operator successfully");
					} catch (OperatorException e) {
						log(LogLevel.ERROR, "applying operator failed: " + e.getMessage());
						return new ParseError(rhsParseResult.getPosition(), e.getMessage(), ErrorType.SEMANTIC_ERROR);
					}
					return new ObjectParseResult(rhsParseResult.getPosition(), operatorResultInfo);
				}
				default:
					return new ParseError(tokenStream.getPosition(), "Internal error: Unknown operator associativity: " + operator.getAssociativity(), ErrorType.INTERNAL_ERROR);
			}
		}
	}

	@Override
	ParseResultIF checkExpectations(ParseResultIF parseResult, ParseExpectation expectation) {
		parseResult = super.checkExpectations(parseResult, expectation);

		if (parseResult.getResultType() != ParseResultType.OBJECT_PARSE_RESULT) {
			// no further checks required
			return parseResult;
		}

		ObjectParseResult objectParseResult = (ObjectParseResult) parseResult;
		List<TypeToken<?>> allowedTypes = expectation.getAllowedTypes();
		TypeToken<?> resultType = parserToolbox.getObjectInfoProvider().getType(objectParseResult.getObjectInfo());
		if (allowedTypes != null && allowedTypes.stream().noneMatch(expectedResultType -> ParseUtils.isConvertibleTo(resultType, expectedResultType))) {
			String messagePrefix = "The class '" + resultType + "' is not assignable to ";
			String messageMiddle = allowedTypes.size() > 1
					? "any of the expected classes "
					: "the expected class ";
			String messageSuffix = "'" + allowedTypes.stream().map(Object::toString).collect(Collectors.joining("', '")) + "'";
			String message = messagePrefix + messageMiddle + messageSuffix;
			log(LogLevel.ERROR, message);
			return new ParseError(parseResult.getPosition(), message, ParseError.ErrorType.SEMANTIC_ERROR);
		}

		return objectParseResult;

	}

	private ObjectInfo applyOperator(ParserToolbox context, ObjectInfo lhs, ObjectInfo rhs, BinaryOperator operator) throws OperatorException {
		return OPERATOR_IMPLEMENTATIONS.get(operator).apply(context.getOperatorResultProvider(), lhs, rhs);
	}

	private boolean stopCircuitEvaluation(ObjectInfo objectInfo, BinaryOperator operator) {
		return operator == BinaryOperator.LOGICAL_AND	&& Boolean.FALSE.equals(objectInfo.getObject())
			|| operator == BinaryOperator.LOGICAL_OR	&& Boolean.TRUE.equals(objectInfo.getObject());
	}

	private ParserToolbox createToolboxWithoutEvaluation() {
		return new ParserToolbox(parserToolbox.getThisInfo(), parserToolbox.getSettings(), ParseMode.WITHOUT_EVALUATION);
	}

	@FunctionalInterface
	private interface OperatorImplementationIF
	{
		ObjectInfo apply(OperatorResultProvider operatorResultProvider, ObjectInfo lhs, ObjectInfo rhs) throws OperatorException;
	}
}
