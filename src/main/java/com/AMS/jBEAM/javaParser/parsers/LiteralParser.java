package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.result.ParseError;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.result.ParseResultType;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

import static com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;

public class LiteralParser extends AbstractEntityParser<ObjectInfo>
{
	private static final ObjectInfo	TRUE_INFO	= new ObjectInfo(true, TypeToken.of(boolean.class));
	private static final ObjectInfo	FALSE_INFO	= new ObjectInfo(false, TypeToken.of(boolean.class));
	private static final ObjectInfo	NULL_INFO	= new ObjectInfo(null, null);

	private final AbstractEntityParser<ObjectInfo> intParser;
	private final AbstractEntityParser<ObjectInfo> longParser;
	private final AbstractEntityParser<ObjectInfo> floatParser;
	private final AbstractEntityParser<ObjectInfo> doubleParser;

	public LiteralParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
		intParser 		= new NumericLiteralParser<>(parserContext, thisInfo, TypeToken.of(int.class),		TokenStream::readIntegerLiteral,	Integer::parseInt,		"Invalid int literal");
		longParser 		= new NumericLiteralParser<>(parserContext, thisInfo, TypeToken.of(long.class),		TokenStream::readLongLiteral, 		Long::parseLong,		"Invalid long literal");
		floatParser 	= new NumericLiteralParser<>(parserContext, thisInfo, TypeToken.of(float.class),	TokenStream::readFloatLiteral,		Float::parseFloat,		"Invalid float literal");
		doubleParser 	= new NumericLiteralParser<>(parserContext, thisInfo, TypeToken.of(double.class),	TokenStream::readDoubleLiteral,		Double::parseDouble,	"Invalid double literal");
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		if (!tokenStream.hasMore()) {
			return new ParseError(tokenStream.getPosition(), "Expected a literal", ErrorType.WRONG_PARSER);
		}
		String characters = tokenStream.peekCharacters();
		if (characters.startsWith("\"")) {
			return parseStringLiteral(tokenStream, expectation);
		} else if (characters.startsWith("'")) {
			return parseCharacterLiteral(tokenStream, expectation);
		} else if (characters.startsWith("true")) {
			return parseNamedLiteral(tokenStream, "true", TRUE_INFO, expectation);
		} else if (characters.startsWith("false")) {
			return parseNamedLiteral(tokenStream, "false", FALSE_INFO, expectation);
		} else if (characters.startsWith("null")) {
			return parseNamedLiteral(tokenStream, "null", NULL_INFO, expectation);
		} else if (characters.startsWith("this")) {
			return parseNamedLiteral(tokenStream, "this", thisInfo, expectation);
		} else {
			return parseNumericLiteral(tokenStream, contextInfo, expectation);
		}
	}

	private ParseResultIF parseStringLiteral(TokenStream tokenStream, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		Token stringLiteralToken;
		try {
			stringLiteralToken = tokenStream.readStringLiteral();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "expected a string literal at " + tokenStream);
			return new ParseError(startPosition, "Expected a string literal", ErrorType.SYNTAX_ERROR);
		}
		if (stringLiteralToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available for string literals");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		String stringLiteralValue = stringLiteralToken.getValue();
		log(LogLevel.SUCCESS, "detected string literal '" + stringLiteralValue + "'");

		ObjectInfo stringLiteralInfo = new ObjectInfo(stringLiteralValue, TypeToken.of(String.class));
		return parserContext.getObjectTailParser().parse(tokenStream, stringLiteralInfo, expectation);
	}

	private ParseResultIF parseCharacterLiteral(TokenStream tokenStream, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		Token characterLiteralToken;
		try {
			characterLiteralToken = tokenStream.readCharacterLiteral();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "expected a character literal at " + tokenStream);
			return new ParseError(startPosition, "Expected a character literal", ErrorType.SYNTAX_ERROR);
		}
		if (characterLiteralToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available for character literals");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		String characterLiteralValue = characterLiteralToken.getValue();
		if (characterLiteralValue.length() != 1) {
			throw new IllegalStateException("Internal error parsing character literals. It should represent exactly 1 character, but it represents " + characterLiteralValue.length());
		}
		log(LogLevel.SUCCESS, "detected character literal '" + characterLiteralValue + "'");

		ObjectInfo stringLiteralInfo = new ObjectInfo(characterLiteralValue.charAt(0), TypeToken.of(char.class));
		return parserContext.getObjectTailParser().parse(tokenStream, stringLiteralInfo, expectation);
	}

	private ParseResultIF parseNamedLiteral(TokenStream tokenStream, String literalName, ObjectInfo literalInfo, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		Token literalToken = tokenStream.readKeyWordUnchecked();
		if (literalToken == null) {
			log(LogLevel.ERROR, "expected literal '" + literalName + "'");
			return new ParseError(startPosition, "Expected '" + literalName + "'", ErrorType.WRONG_PARSER);
		}
		if (literalToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		if (!literalToken.getValue().equals(literalName)) {
			log(LogLevel.ERROR, "expected literal '" + literalName + "'");
			return new ParseError(startPosition, "Expected '" + literalName + "'", ErrorType.WRONG_PARSER);
		}
		log(LogLevel.SUCCESS, "detected literal '" + literalName + "'");
		return parserContext.getObjectTailParser().parse(tokenStream, literalInfo, expectation);
	}

	private ParseResultIF parseNumericLiteral(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		char c = tokenStream.peekCharacter();
		if (!"+-.0123456789".contains(String.valueOf(c))) {
			log(LogLevel.ERROR, "expected a numeric literal");
			return new ParseError(startPosition, "Expected a literal", ErrorType.WRONG_PARSER);
		}

		AbstractEntityParser[] parsers = { longParser, intParser, floatParser, doubleParser };
		for (AbstractEntityParser parser : parsers) {
			ParseResultIF parseResult = parser.parse(tokenStream, contextInfo, expectation);

			if (parseResult.getResultType() != ParseResultType.PARSE_ERROR) {
				return parseResult;
			}
		}
		log(LogLevel.ERROR, "expected a numeric literal");
		return new ParseError(startPosition, "Expected a numeric literal", ErrorType.WRONG_PARSER);
	}

	private static class NumericLiteralParser<T> extends AbstractEntityParser<ObjectInfo>
	{
		private final TypeToken<T>			numericType;
		private final NumericTokenReader	tokenReader;
		private final NumericValueParser<T>	valueParser;
		private final String				wrongTypeError;

		NumericLiteralParser(ParserContext parserContext, ObjectInfo thisInfo, TypeToken<T> numericType, NumericTokenReader tokenReader, NumericValueParser<T> valueParser, String wrongTypeError) {
			super(parserContext, thisInfo);
			this.numericType = numericType;
			this.tokenReader = tokenReader;
			this.valueParser = valueParser;
			this.wrongTypeError = wrongTypeError;
		}

		@Override
		ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
			int startPosition = tokenStream.getPosition();
			Token token;
			try {
				token = tokenReader.read(tokenStream);
			} catch (TokenStream.JavaTokenParseException e) {
				return new ParseError(startPosition, wrongTypeError, ErrorType.WRONG_PARSER);
			}
			if (token.isContainsCaret()) {
				log(LogLevel.INFO, "no completion suggestions available");
				return CompletionSuggestions.none(tokenStream.getPosition());
			}

			T literalValue;
			try {
				literalValue = valueParser.parse(token.getValue());
				log(LogLevel.SUCCESS, "detected numeric literal '" + token.getValue() + "'");
			} catch (NumberFormatException e) {
				log(LogLevel.ERROR, "number format exception: " + e.getMessage());
				return new ParseError(startPosition, wrongTypeError, ErrorType.SEMANTIC_ERROR);
			}

			ObjectInfo literalInfo = new ObjectInfo(literalValue, numericType);
			return parserContext.getObjectTailParser().parse(tokenStream, literalInfo, expectation);
		}
	}

	@FunctionalInterface
	private interface NumericTokenReader
	{
		Token read(TokenStream tokenStream) throws TokenStream.JavaTokenParseException;
	}

	@FunctionalInterface
	private interface NumericValueParser<T>
	{
		T parse(String s) throws NumberFormatException;
	}
}
