package com.AMS.jBEAM.javaParser;

import java.util.Collections;

class JavaLiteralParser extends AbstractJavaEntityParser
{
	JavaLiteralParser(JavaParserPool parserSettings, ObjectInfo thisInfo) {
		super(parserSettings, thisInfo);
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, Class<?> expectedResultClass) {
		int startPosition = tokenStream.getPosition();
		if (!tokenStream.hasMore()) {
			return new ParseError(startPosition, "Expected a literal");
		}
		char c = tokenStream.peekCharacter();
		switch (c) {
			case '\"':
				return parseStringLiteral(tokenStream, expectedResultClass);
			case '\'':
				return parseCharacterLiteral(tokenStream, expectedResultClass);
			case 't':
				return parseNamedLiteral(tokenStream, true, expectedResultClass);
			case 'f':
				return parseNamedLiteral(tokenStream, false, expectedResultClass);
			case 'n':
				return parseNamedLiteral(tokenStream, null, expectedResultClass);
			default: {
				if (!"+-.0123456789".contains(String.valueOf(c))) {
					return new ParseError(startPosition, "Expected a literal");
				}
				ParseResultIF longParseResult = parseLongLiteral(tokenStream.clone(), expectedResultClass);
				if (longParseResult.getResultType() != ParseResultType.PARSE_ERROR) {
					return longParseResult;
				}
				ParseResultIF integerParseResult = parseIntegerLiteral(tokenStream.clone(), expectedResultClass);
				if (integerParseResult.getResultType() != ParseResultType.PARSE_ERROR) {
					return integerParseResult;
				}
				ParseResultIF floatParseResult = parseFloatLiteral(tokenStream.clone(), expectedResultClass);
				if (floatParseResult.getResultType() != ParseResultType.PARSE_ERROR) {
					return floatParseResult;
				}
				ParseResultIF doubleParseResult = parseDoubleLiteral(tokenStream.clone(), expectedResultClass);
				if (doubleParseResult.getResultType() != ParseResultType.PARSE_ERROR) {
					return doubleParseResult;
				}
				return new ParseError(startPosition, "Expected a numeric literal");
			}
		}
	}

	private ParseResultIF parseStringLiteral(JavaTokenStream tokenStream, Class<?> expectedResultClass) {
		int startPosition = tokenStream.getPosition();
		JavaToken stringLiteralToken;
		try {
			stringLiteralToken = tokenStream.readStringLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a string literal");
		}
		if (stringLiteralToken.isContainsCaret()) {
			// No suggestions possible
			return new CompletionSuggestions(Collections.emptyMap());
		}
		String stringLiteralValue = stringLiteralToken.getValue();
		ObjectInfo stringLiteralInfo = new ObjectInfo(stringLiteralValue);
		return parserPool.getObjectTailParser().parse(tokenStream, stringLiteralInfo, expectedResultClass);
	}

	private ParseResultIF parseCharacterLiteral(JavaTokenStream tokenStream, Class<?> expectedResultClass) {
		int startPosition = tokenStream.getPosition();
		JavaToken characterLiteralToken;
		try {
			characterLiteralToken = tokenStream.readCharacterLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a character literal");
		}
		if (characterLiteralToken.isContainsCaret()) {
			// No suggestions possible
			return new CompletionSuggestions(Collections.emptyMap());
		}
		String characterLiteralValue = characterLiteralToken.getValue();
		if (characterLiteralValue.length() != 1) {
			throw new IllegalStateException("Internal error parsing character literals. It should represent exactly 1 character, but it represents " + characterLiteralValue.length());
		}
		ObjectInfo stringLiteralInfo = new ObjectInfo(characterLiteralValue.charAt(0));
		return parserPool.getObjectTailParser().parse(tokenStream, stringLiteralInfo, expectedResultClass);
	}

	private ParseResultIF parseNamedLiteral(JavaTokenStream tokenStream, Object namedLiteral, Class<?> expectedResultClass) {
		String literalName = namedLiteral == null ? "null" : namedLiteral.toString();

		int startPosition = tokenStream.getPosition();
		JavaToken literalToken;
		try {
			literalToken = tokenStream.readNamedLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected '" + literalName + "'");
		}
		if (literalToken.isContainsCaret()) {
			// No suggestions possible
			return new CompletionSuggestions(Collections.emptyMap());
		}
		if (!literalToken.getValue().equals(literalName)) {
			return new ParseError(startPosition, "Expected '" + literalName + "'");
		}
		ObjectInfo namedLiteralInfo = new ObjectInfo(namedLiteral);
		return parserPool.getObjectTailParser().parse(tokenStream, namedLiteralInfo, expectedResultClass);
	}

	private ParseResultIF parseLongLiteral(JavaTokenStream tokenStream, Class<?> expectedResultClass) {
		int startPosition = tokenStream.getPosition();
		JavaToken longToken;
		try {
			longToken = tokenStream.readLongLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected an integer literal");
		}
		if (longToken.isContainsCaret()) {
			// No suggestions possible
			return new CompletionSuggestions(Collections.emptyMap());
		}

		long literalValue;
		try {
			literalValue = Long.parseLong(longToken.getValue());
		} catch (NumberFormatException e) {
			return new ParseError(startPosition, "Invalid long literal");
		}

		ObjectInfo longLiteralInfo = new ObjectInfo(literalValue);
		return parserPool.getObjectTailParser().parse(tokenStream, longLiteralInfo, expectedResultClass);
	}

	private ParseResultIF parseIntegerLiteral(JavaTokenStream tokenStream, Class<?> expectedResultClass) {
		int startPosition = tokenStream.getPosition();
		JavaToken integerToken;
		try {
			integerToken = tokenStream.readIntegerLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected an integer literal");
		}
		if (integerToken.isContainsCaret()) {
			// No suggestions possible
			return new CompletionSuggestions(Collections.emptyMap());
		}

		int literalValue;
		try {
			literalValue = Integer.parseInt(integerToken.getValue());
		} catch (NumberFormatException e) {
			return new ParseError(startPosition, "Invalid integer literal");
		}

		final Object literalValueWithSmallestPossibleDataType;
		if (Byte.MIN_VALUE <= literalValue && literalValue <= Byte.MAX_VALUE) {
			literalValueWithSmallestPossibleDataType = (byte) literalValue;
		} else if (Short.MIN_VALUE <= literalValue && literalValue <= Short.MAX_VALUE) {
			literalValueWithSmallestPossibleDataType = (short) literalValue;
		} else {
			literalValueWithSmallestPossibleDataType = literalValue;
		}

		ObjectInfo integerLiteralInfo = new ObjectInfo(literalValueWithSmallestPossibleDataType);
		return parserPool.getObjectTailParser().parse(tokenStream, integerLiteralInfo, expectedResultClass);
	}

	private ParseResultIF parseFloatLiteral(JavaTokenStream tokenStream, Class<?> expectedResultClass) {
		int startPosition = tokenStream.getPosition();
		JavaToken floatToken;
		try {
			floatToken = tokenStream.readFloatLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a float literal");
		}
		if (floatToken.isContainsCaret()) {
			// No suggestions possible
			return new CompletionSuggestions(Collections.emptyMap());
		}

		float literalValue;
		try {
			literalValue = Float.parseFloat(floatToken.getValue());
		} catch (NumberFormatException e) {
			return new ParseError(startPosition, "Invalid float literal");
		}
		ObjectInfo floatLiteralInfo = new ObjectInfo(literalValue);
		return parserPool.getObjectTailParser().parse(tokenStream, floatLiteralInfo, expectedResultClass);
	}

	private ParseResultIF parseDoubleLiteral(JavaTokenStream tokenStream, Class<?> expectedResultClass) {
		int startPosition = tokenStream.getPosition();
		JavaToken doubleToken;
		try {
			doubleToken = tokenStream.readDoubleLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a double literal");
		}
		if (doubleToken.isContainsCaret()) {
			// No suggestions possible
			return new CompletionSuggestions(Collections.emptyMap());
		}

		double literalValue;
		try {
			literalValue = Double.parseDouble(doubleToken.getValue());
		} catch (NumberFormatException e) {
			return new ParseError(startPosition, "Invalid double literal");
		}
		ObjectInfo doubleLiteralInfo = new ObjectInfo(literalValue);
		return parserPool.getObjectTailParser().parse(tokenStream, doubleLiteralInfo, expectedResultClass);
	}
}
