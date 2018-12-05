package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.result.ParseError;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.result.ParseResultType;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;

import java.util.List;

import static com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;

public class LiteralParser extends AbstractEntityParser
{
	private final AbstractEntityParser intParser;
	private final AbstractEntityParser longParser;
	private final AbstractEntityParser floatParser;
	private final AbstractEntityParser doubleParser;

	public LiteralParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
		intParser 		= new NumericLiteralParser<>(parserContext, thisInfo, int.class,	TokenStream::readIntegerLiteral,	Integer::parseInt,		"Invalid int literal");
		longParser 		= new NumericLiteralParser<>(parserContext, thisInfo, long.class,	TokenStream::readLongLiteral, 		Long::parseLong,		"Invalid long literal");
		floatParser 	= new NumericLiteralParser<>(parserContext, thisInfo, float.class,	TokenStream::readFloatLiteral,		Float::parseFloat,		"Invalid float literal");
		doubleParser 	= new NumericLiteralParser<>(parserContext, thisInfo, double.class,	TokenStream::readDoubleLiteral,		Double::parseDouble,	"Invalid double literal");
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		if (!tokenStream.hasMore()) {
			return new ParseError(tokenStream.getPosition(), "Expected a literal", ErrorType.WRONG_PARSER);
		}
		String characters = tokenStream.peekCharacters();
		if (characters.startsWith("\"")) {
			return parseStringLiteral(tokenStream, expectedResultClasses);
		} else if (characters.startsWith("'")) {
			return parseCharacterLiteral(tokenStream, expectedResultClasses);
		} else if (characters.startsWith("true")) {
			return parseNamedLiteral(tokenStream, "true", true, boolean.class, expectedResultClasses);
		} else if (characters.startsWith("false")) {
			return parseNamedLiteral(tokenStream, "false", false, boolean.class, expectedResultClasses);
		} else if (characters.startsWith("null")) {
			return parseNamedLiteral(tokenStream, "null", null, null, expectedResultClasses);
		} else if (characters.startsWith("this")) {
			return parseNamedLiteral(tokenStream, "this", thisInfo.getObject(), thisInfo.getDeclaredClass(), expectedResultClasses);
		} else {
			return parseNumericLiteral(tokenStream, currentContextInfo, expectedResultClasses);
		}
	}

	private ParseResultIF parseStringLiteral(TokenStream tokenStream, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		Token stringLiteralToken;
		try {
			stringLiteralToken = tokenStream.readStringLiteral();
		} catch (TokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a string literal", ErrorType.SYNTAX_ERROR);
		}
		if (stringLiteralToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}
		String stringLiteralValue = stringLiteralToken.getValue();
		ObjectInfo stringLiteralInfo = new ObjectInfo(stringLiteralValue);
		return parserContext.getTailParser(false).parse(tokenStream, stringLiteralInfo, expectedResultClasses);
	}

	private ParseResultIF parseCharacterLiteral(TokenStream tokenStream, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		Token characterLiteralToken;
		try {
			characterLiteralToken = tokenStream.readCharacterLiteral();
		} catch (TokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a character literal", ErrorType.SYNTAX_ERROR);
		}
		if (characterLiteralToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}
		String characterLiteralValue = characterLiteralToken.getValue();
		if (characterLiteralValue.length() != 1) {
			throw new IllegalStateException("Internal error parsing character literals. It should represent exactly 1 character, but it represents " + characterLiteralValue.length());
		}
		ObjectInfo stringLiteralInfo = new ObjectInfo(characterLiteralValue.charAt(0), char.class);
		return parserContext.getTailParser(false).parse(tokenStream, stringLiteralInfo, expectedResultClasses);
	}

	private ParseResultIF parseNamedLiteral(TokenStream tokenStream, String literalName, Object literalValue, Class<?> literalClass, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		Token literalToken = tokenStream.readKeyWordUnchecked();
		if (literalToken == null) {
			return new ParseError(startPosition, "Expected '" + literalName + "'", ErrorType.WRONG_PARSER);
		}
		if (literalToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}
		if (!literalToken.getValue().equals(literalName)) {
			return new ParseError(startPosition, "Expected '" + literalName + "'", ErrorType.WRONG_PARSER);
		}
		ObjectInfo namedLiteralInfo = new ObjectInfo(literalValue, literalClass);
		return parserContext.getTailParser(false).parse(tokenStream, namedLiteralInfo, expectedResultClasses);
	}

	private ParseResultIF parseNumericLiteral(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		char c = tokenStream.peekCharacter();
		if (!"+-.0123456789".contains(String.valueOf(c))) {
			return new ParseError(startPosition, "Expected a literal", ErrorType.WRONG_PARSER);
		}

		AbstractEntityParser[] parsers = { longParser, intParser, floatParser, doubleParser };
		for (AbstractEntityParser parser : parsers) {
			ParseResultIF parseResult = parser.parse(tokenStream, currentContextInfo, expectedResultClasses);
			if (parseResult.getResultType() != ParseResultType.PARSE_ERROR) {
				return parseResult;
			}
		}
		return new ParseError(startPosition, "Expected a numeric literal", ErrorType.WRONG_PARSER);
	}

	private static class NumericLiteralParser<T> extends AbstractEntityParser
	{
		private final Class<T>				numericType;
		private final NumericTokenReader	tokenReader;
		private final NumericValueParser<T>	valueParser;
		private final String				wrongTypeError;

		NumericLiteralParser(ParserContext parserContext, ObjectInfo thisInfo, Class<T> numericType, NumericTokenReader tokenReader, NumericValueParser<T> valueParser, String wrongTypeError) {
			super(parserContext, thisInfo);
			this.numericType = numericType;
			this.tokenReader = tokenReader;
			this.valueParser = valueParser;
			this.wrongTypeError = wrongTypeError;
		}

		@Override
		ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
			int startPosition = tokenStream.getPosition();
			Token token;
			try {
				token = tokenReader.read(tokenStream);
			} catch (TokenStream.JavaTokenParseException e) {
				return new ParseError(startPosition, wrongTypeError, ErrorType.WRONG_PARSER);
			}
			if (token.isContainsCaret()) {
				// No suggestions possible
				return CompletionSuggestions.NONE;
			}

			T literalValue;
			try {
				literalValue = valueParser.parse(token.getValue());
			} catch (NumberFormatException e) {
				return new ParseError(startPosition, wrongTypeError, ErrorType.SEMANTIC_ERROR);
			}

			ObjectInfo literalInfo = new ObjectInfo(literalValue, numericType);
			return parserContext.getTailParser(false).parse(tokenStream, literalInfo, expectedResultClasses);
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
