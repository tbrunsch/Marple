package com.AMS.jBEAM.javaParser;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JavaTokenStream implements Cloneable
{
	private static final Pattern	CHARACTER_PATTERN			= Pattern.compile("^(\\s*([^\\s])\\s*).*");
	private static final Pattern	IDENTIFIER_PATTERN  		= Pattern.compile("^(\\s*([A-Za-z][_A-Za-z0-9]*)\\s*).*");
	private static final Pattern	STRING_LITERAL_PATTERN		= Pattern.compile("^(\\s*\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"\\s*).*");
	private static final Pattern	CHARACTER_LITERAL_PATTERN	= Pattern.compile("^(\\s*'(\\\\.|[^\\\\])'\\s*).*");
	private static final Pattern	NAMED_LITERAL_PATTERN		= IDENTIFIER_PATTERN;
	private static final Pattern	INTEGER_LITERAL_PATTERN		= Pattern.compile("^(\\s*(0|-?[1-9][0-9]*)\\s*)($|[^0-9dDeEfFL].*)");
	private static final Pattern	LONG_LITERAL_PATTERN		= Pattern.compile("^(\\s*(0|-?[1-9][0-9]*)[lL]\\s*).*");
	private static final Pattern	FLOAT_LITERAL_PATTERN 		= Pattern.compile("^(\\s*([+-]?([0-9]+([eE][+-]?[0-9]+)?|\\.[0-9]+([eE][+-]?[0-9]+)?|[0-9]+\\.[0-9]*([eE][+-]?[0-9]+)?)[fF])\\s*).*");
	private static final Pattern	DOUBLE_LITERAL_PATTERN 		= Pattern.compile("^(\\s*([+-]?([0-9]+(([eE][+-]?[0-9]+)?[dD]|[eE][+-]?[0-9]+[dD]?)|\\.[0-9]+([eE][+-]?[0-9]+)?[dD]?|[0-9]+\\.[0-9]*([eE][+-]?[0-9]+)?[dD]?))\\s*).*");

	private static final Map<Character, Character> INTERPRETATION_OF_ESCAPED_CHARACTERS = ImmutableMap.<Character, Character>builder()
		.put('t', '\t')
		.put('b', '\b')
		.put('n', '\n')
		.put('r', '\r')
		.put('f', '\f')
		.put('\'', '\'')
		.put('\"', '\"')
		.put('\\', '\\')
		.build();

	private static String unescapeCharacters(String s) throws IllegalArgumentException {
		StringBuffer unescapedString = new StringBuffer();
		int pos = 0;
		while (pos < s.length()) {
			char c = s.charAt(pos);
			if (c == '\\') {
				if (pos + 1 == s.length()) {
					throw new IllegalArgumentException("String ends with backslash '\\'");
				}
				char escapedChar = s.charAt(pos + 1);
				Character interpretation = INTERPRETATION_OF_ESCAPED_CHARACTERS.get(escapedChar);
				if (interpretation == null) {
					throw new IllegalArgumentException("Unknown escape sequence: \\" + escapedChar);
				}
				unescapedString.append((char) interpretation);
				pos += 2;
			} else {
				unescapedString.append(c);
				pos++;
			}
		}
		return unescapedString.toString();
	}

	private final String	javaExpression;
	private final int	   caret;

	private int			 position;

	JavaTokenStream(String javaExpression, int caret) {
		this(javaExpression, caret, 0);
	}

	private JavaTokenStream(String javaExpression, int caret, int position) {
		this.javaExpression = javaExpression;
		this.caret = caret;
		this.position = position;
	}

	boolean hasMore() {
		return position < javaExpression.length() && CHARACTER_PATTERN.matcher(javaExpression.substring(position)).matches();
	}

	int getPosition() {
		return position;
	}

	JavaToken readIdentifier() throws JavaTokenParseException {
		return readRegex(IDENTIFIER_PATTERN, 2, "No identifier found");
	}

	JavaToken readStringLiteral() throws JavaTokenParseException {
		JavaToken escapedStringLiteralToken = readRegex(STRING_LITERAL_PATTERN, 2, "No string literal found");
		return unescapeStringToken(escapedStringLiteralToken);
	}

	JavaToken readCharacterLiteral() throws JavaTokenParseException {
		JavaToken escapedCharacterLiteralToken = readRegex(CHARACTER_LITERAL_PATTERN, 2, "No character literal found");
		return unescapeStringToken(escapedCharacterLiteralToken);
	}

	JavaToken readNamedLiteral() throws JavaTokenParseException {
		return readRegex(NAMED_LITERAL_PATTERN, 2, "No named literal found");
	}

	JavaToken readIntegerLiteral() throws JavaTokenParseException {
		return readRegex(INTEGER_LITERAL_PATTERN, 2, "No integer literal found");
	}

	JavaToken readLongLiteral() throws JavaTokenParseException {
		return readRegex(LONG_LITERAL_PATTERN, 2, "No long literal found");
	}

	JavaToken readFloatLiteral() throws JavaTokenParseException {
		return readRegex(FLOAT_LITERAL_PATTERN, 2, "No float literal found");
	}

	JavaToken readDoubleLiteral() throws JavaTokenParseException {
		return readRegex(DOUBLE_LITERAL_PATTERN, 2, "No double literal found");
	}

	private JavaToken unescapeStringToken(JavaToken stringToken) throws JavaTokenParseException {
		String escapedString = stringToken.getValue();
		try {
			String unescapedString = unescapeCharacters(escapedString);
			return new JavaToken(unescapedString, stringToken.isContainsCaret());
		} catch (IllegalArgumentException e) {
			throw new JavaTokenParseException(e.getMessage());
		}
	}

	private JavaToken readRegex(Pattern regex, int groupIndexToExtract, String errorMessage) throws JavaTokenParseException {
		Matcher matcher = regex.matcher(javaExpression.substring(position));
		if (!matcher.matches()) {
			throw new JavaTokenParseException(errorMessage);
		}
		String extractedString = matcher.group(groupIndexToExtract);
		String stringWithSpaces = matcher.group(1);
		int length = stringWithSpaces.length();
		boolean containsCaret = moveForward(length);
		return new JavaToken(extractedString, containsCaret);
	}

	char peekCharacter() {
		Matcher matcher = CHARACTER_PATTERN.matcher(javaExpression.substring(position));
		if (!matcher.matches()) {
			return 0;
		}
		String character = matcher.group(2);
		return character.charAt(0);
	}

	JavaToken readCharacter() throws JavaTokenParseException {
		return readRegex(CHARACTER_PATTERN, 2, null);
	}

	JavaToken readCharacterUnchecked() {
		try {
			return readCharacter();
		} catch (JavaTokenParseException e) {
			return null;
		}
	}

	/**
	 * Returns true whether the position strived the caret when moving from position to nextPosition
	 */
	private boolean moveForward(int numCharacters) {
		int newPosition = position + numCharacters;
		boolean containsCaret = position < caret && caret <= newPosition;
		moveTo(newPosition);
		return containsCaret;
	}

	void moveTo(int newPosition) {
		position = newPosition;
	}

	@Override
	public JavaTokenStream clone() {
		return new JavaTokenStream(javaExpression, caret, position);
	}

	static class JavaTokenParseException extends Exception
	{
		JavaTokenParseException(String message) {
			super(message);
		}
	}
}
