package com.AMS.jBEAM.javaParser;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JavaTokenStream implements Cloneable
{
    // TODO: Support white spaces
    private static final Pattern    IDENTIFIER_PATTERN  		= Pattern.compile("^([A-Za-z][_A-Za-z0-9]*).*");
    private static final Pattern	STRING_LITERAL_PATTERN		= Pattern.compile("^\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\".*");
    private static final Pattern	CHARACTER_LITERAL_PATTERN	= Pattern.compile("^'(\\\\.|[^\\\\])'.*");

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

	private final String    javaExpression;
    private final int       caret;

    private int             position;

    JavaTokenStream(String javaExpression, int caret) {
        this(javaExpression, caret, 0);
    }

    private JavaTokenStream(String javaExpression, int caret, int position) {
        this.javaExpression = javaExpression;
        this.caret = caret;
        this.position = position;
    }

    boolean hasMore() {
        return position < javaExpression.length();
    }

    int getPosition() {
        return position;
    }

    JavaToken readIdentifier() throws JavaTokenParseException {
    	return readRegex(IDENTIFIER_PATTERN, 1, 0, "No identifier found");
    }

    JavaToken readStringLiteral() throws JavaTokenParseException {
    	JavaToken escapedStringLiteralToken = readRegex(STRING_LITERAL_PATTERN, 1, 2, "No string literal found");
		return unescapeStringToken(escapedStringLiteralToken);
	}

	JavaToken readCharacterLiteral() throws JavaTokenParseException {
		JavaToken escapedCharacterLiteralToken = readRegex(CHARACTER_LITERAL_PATTERN, 1, 2, "No character literal found");
		return unescapeStringToken(escapedCharacterLiteralToken);
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

	private JavaToken readRegex(Pattern regex, int groupIndexToExtract, int tokenLengthCorrection, String errorMessage) throws JavaTokenParseException {
		Matcher matcher = regex.matcher(javaExpression.substring(position));
		if (!matcher.matches()) {
			throw new JavaTokenParseException(errorMessage);
		}
		String extractedString = matcher.group(groupIndexToExtract);
		// TODO: Only correct if no white spaces
		int length = extractedString.length() + tokenLengthCorrection;
		boolean containsCaret = moveForward(length);
		return new JavaToken(extractedString, containsCaret);
	}

    char peekCharacter() {
        return javaExpression.charAt(position);
    }

    JavaToken readCharacter() {
        char c = peekCharacter();
        boolean containsCaret = moveForward(1);
        return new JavaToken(String.valueOf(c), containsCaret);
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
