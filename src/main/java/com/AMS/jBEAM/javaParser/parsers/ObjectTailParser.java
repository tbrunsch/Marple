package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.result.ObjectParseResult;
import com.AMS.jBEAM.javaParser.result.ParseError;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

/**
 * Parses a sub expression following a complete Java expression, assuming the context
 * <ul>
 *     <li>{@code <object>.} or</li>
 *     <li>{@code <object>[] or</li>
 * </ul>
 */
public class ObjectTailParser extends AbstractTailParser<ObjectInfo>
{
	public ObjectTailParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF parseDot(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals(".");

		AbstractEntityParser<ObjectInfo> fieldParser = parserToolbox.getObjectFieldParser();
		AbstractEntityParser<ObjectInfo> methodParser = parserToolbox.getObjectMethodParser();
		return ParseUtils.parse(tokenStream, contextInfo, expectation,
			fieldParser,
			methodParser
		);
	}

	@Override
	ParseResultIF parseOpeningSquareBracket(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		// array access
		TypeToken<?> currentContextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		TypeToken<?> elementType = currentContextType.getComponentType();
		if (elementType == null) {
			log(LogLevel.ERROR, "cannot apply operator [] for non-array types");
			return new ParseError(tokenStream.getPosition(), "Cannot apply [] to non-array types", ParseError.ErrorType.SEMANTIC_ERROR);
		}

		int indexStartPosition = tokenStream.getPosition();
		ParseExpectation indexExpectation = ParseExpectationBuilder.expectObject().allowedType(TypeToken.of(int.class)).build();
		ParseResultIF arrayIndexParseResult = parseArrayIndex(tokenStream, indexExpectation);

		if (ParseUtils.propagateParseResult(arrayIndexParseResult, indexExpectation)) {
			return arrayIndexParseResult;
		}

		ObjectParseResult parseResult = (ObjectParseResult) arrayIndexParseResult;
		int parsedToPosition = parseResult.getPosition();
		ObjectInfo indexInfo = parseResult.getObjectInfo();
		ObjectInfo elementInfo;
		try {
			elementInfo = parserToolbox.getObjectInfoProvider().getArrayElementInfo(contextInfo, indexInfo);
			log(LogLevel.SUCCESS, "detected valid array access");
		} catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
			log(LogLevel.ERROR, "caught exception: " + e.getMessage());
			return new ParseError(indexStartPosition, e.getClass().getSimpleName() + " during array index evaluation", ParseError.ErrorType.EVALUATION_EXCEPTION, e);
		}
		tokenStream.moveTo(parsedToPosition);
		return parserToolbox.getObjectTailParser().parse(tokenStream, elementInfo, expectation);

	}

	@Override
	ParseResultIF createParseResult(int position, ObjectInfo objectInfo) {
		return new ObjectParseResult(position, objectInfo);
	}

	private ParseResultIF parseArrayIndex(TokenStream tokenStream, ParseExpectation expectation) {
		log(LogLevel.INFO, "parsing array index");

		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");

		ParseResultIF arrayIndexParseResult = parserToolbox.getRootParser().parse(tokenStream, thisInfo, expectation);

		if (ParseUtils.propagateParseResult(arrayIndexParseResult, expectation)) {
			return arrayIndexParseResult;
		}

		ObjectParseResult parseResult = ((ObjectParseResult) arrayIndexParseResult);
		int parsedToPosition = parseResult.getPosition();

		tokenStream.moveTo(parsedToPosition);
		characterToken = tokenStream.readCharacterUnchecked();

		if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
			log(LogLevel.ERROR, "missing ']' at " + tokenStream);
			return new ParseError(parsedToPosition, "Expected closing bracket ']'", ParseError.ErrorType.SYNTAX_ERROR);
		}

		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		return new ObjectParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
	}
}
