package com.AMS.jBEAM.javaParser;

import java.util.List;

class JavaLiteralParser extends AbstractJavaEntityParser
{
	JavaLiteralParser(JavaParserPool parserSettings, ObjectInfo thisInfo) {
		super(parserSettings, thisInfo);
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		if (!tokenStream.hasMore()) {
			return new ParseError(startPosition, "Expected a literal");
		}
		char c = tokenStream.peekCharacter();
		switch (c) {
			case '\"':
				return parseStringLiteral(tokenStream, expectedResultClasses);
			case '\'':
				return parseCharacterLiteral(tokenStream, expectedResultClasses);
			case 't': {
				JavaTokenStream tempTokenStream = tokenStream.clone();
				tempTokenStream.readCharacterUnchecked();
				if (tempTokenStream.hasMore()) {
					c = tempTokenStream.peekCharacter();
					if (c == 'r') {
						return parseNamedLiteral(tokenStream, "true", true, boolean.class, expectedResultClasses);
					} else if (c == 'h') {
						return parseNamedLiteral(tokenStream, "this", thisInfo.getObject(), thisInfo.getDeclaredClass(), expectedResultClasses);
					}
				}
				return new ParseError(startPosition, "Expected a literal");
			}
			case 'f':
				return parseNamedLiteral(tokenStream, "false", false, boolean.class, expectedResultClasses);
			case 'n':
				return parseNamedLiteral(tokenStream, "null", null, null, expectedResultClasses);
			default: {
				if (!"+-.0123456789".contains(String.valueOf(c))) {
					return new ParseError(startPosition, "Expected a literal");
				}
				ParseResultIF longParseResult = parseLongLiteral(tokenStream.clone(), expectedResultClasses);
				if (longParseResult.getResultType() != ParseResultType.PARSE_ERROR) {
					return longParseResult;
				}
				ParseResultIF integerParseResult = parseIntegerLiteral(tokenStream.clone(), expectedResultClasses);
				if (integerParseResult.getResultType() != ParseResultType.PARSE_ERROR) {
					return integerParseResult;
				}
				ParseResultIF floatParseResult = parseFloatLiteral(tokenStream.clone(), expectedResultClasses);
				if (floatParseResult.getResultType() != ParseResultType.PARSE_ERROR) {
					return floatParseResult;
				}
				ParseResultIF doubleParseResult = parseDoubleLiteral(tokenStream.clone(), expectedResultClasses);
				if (doubleParseResult.getResultType() != ParseResultType.PARSE_ERROR) {
					return doubleParseResult;
				}
				return new ParseError(startPosition, "Expected a numeric literal");
			}
		}
	}

	private ParseResultIF parseStringLiteral(JavaTokenStream tokenStream, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		JavaToken stringLiteralToken;
		try {
			stringLiteralToken = tokenStream.readStringLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a string literal");
		}
		if (stringLiteralToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}
		String stringLiteralValue = stringLiteralToken.getValue();
		ObjectInfo stringLiteralInfo = new ObjectInfo(stringLiteralValue);
		return parserPool.getObjectTailParser().parse(tokenStream, stringLiteralInfo, expectedResultClasses);
	}

	private ParseResultIF parseCharacterLiteral(JavaTokenStream tokenStream, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		JavaToken characterLiteralToken;
		try {
			characterLiteralToken = tokenStream.readCharacterLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a character literal");
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
		return parserPool.getObjectTailParser().parse(tokenStream, stringLiteralInfo, expectedResultClasses);
	}

	private ParseResultIF parseNamedLiteral(JavaTokenStream tokenStream, String literalName, Object literalValue, Class<?> literalClass, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		JavaToken literalToken;
		try {
			literalToken = tokenStream.readNamedLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected '" + literalName + "'");
		}
		if (literalToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}
		if (!literalToken.getValue().equals(literalName)) {
			return new ParseError(startPosition, "Expected '" + literalName + "'");
		}
		ObjectInfo namedLiteralInfo = new ObjectInfo(literalValue, literalClass);
		return parserPool.getObjectTailParser().parse(tokenStream, namedLiteralInfo, expectedResultClasses);
	}

	private ParseResultIF parseLongLiteral(JavaTokenStream tokenStream, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		JavaToken longToken;
		try {
			longToken = tokenStream.readLongLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected an integer literal");
		}
		if (longToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}

		long literalValue;
		try {
			literalValue = Long.parseLong(longToken.getValue());
		} catch (NumberFormatException e) {
			return new ParseError(startPosition, "Invalid long literal");
		}

		ObjectInfo longLiteralInfo = new ObjectInfo(literalValue, long.class);
		return parserPool.getObjectTailParser().parse(tokenStream, longLiteralInfo, expectedResultClasses);
	}

	private ParseResultIF parseIntegerLiteral(JavaTokenStream tokenStream, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		JavaToken integerToken;
		try {
			integerToken = tokenStream.readIntegerLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected an integer literal");
		}
		if (integerToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}

		int literalValue;
		try {
			literalValue = Integer.parseInt(integerToken.getValue());
		} catch (NumberFormatException e) {
			return new ParseError(startPosition, "Invalid integer literal");
		}

		final ObjectInfo integerLiteralInfo;
		if (Byte.MIN_VALUE <= literalValue && literalValue <= Byte.MAX_VALUE) {
			integerLiteralInfo = new ObjectInfo((byte) literalValue, byte.class);
		} else if (Short.MIN_VALUE <= literalValue && literalValue <= Short.MAX_VALUE) {
			integerLiteralInfo = new ObjectInfo((short) literalValue, short.class);
		} else {
			integerLiteralInfo = new ObjectInfo(literalValue, int.class);
		}
		return parserPool.getObjectTailParser().parse(tokenStream, integerLiteralInfo, expectedResultClasses);
	}

	private ParseResultIF parseFloatLiteral(JavaTokenStream tokenStream, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		JavaToken floatToken;
		try {
			floatToken = tokenStream.readFloatLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a float literal");
		}
		if (floatToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}

		float literalValue;
		try {
			literalValue = Float.parseFloat(floatToken.getValue());
		} catch (NumberFormatException e) {
			return new ParseError(startPosition, "Invalid float literal");
		}
		ObjectInfo floatLiteralInfo = new ObjectInfo(literalValue, float.class);
		return parserPool.getObjectTailParser().parse(tokenStream, floatLiteralInfo, expectedResultClasses);
	}

	private ParseResultIF parseDoubleLiteral(JavaTokenStream tokenStream, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		JavaToken doubleToken;
		try {
			doubleToken = tokenStream.readDoubleLiteral();
		} catch (JavaTokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected a double literal");
		}
		if (doubleToken.isContainsCaret()) {
			// No suggestions possible
			return CompletionSuggestions.NONE;
		}

		double literalValue;
		try {
			literalValue = Double.parseDouble(doubleToken.getValue());
		} catch (NumberFormatException e) {
			return new ParseError(startPosition, "Invalid double literal");
		}
		ObjectInfo doubleLiteralInfo = new ObjectInfo(literalValue, double.class);
		return parserPool.getObjectTailParser().parse(tokenStream, doubleLiteralInfo, expectedResultClasses);
	}
}
