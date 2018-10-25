package com.AMS.jBEAM.javaParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JavaTokenStream implements Cloneable
{
    // TODO: Support white spaces
    private static final Pattern    IDENTIFIER_PATTERN  = Pattern.compile("^([A-Za-z][_A-Za-z0-9]*).*");

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
        Matcher matcher = IDENTIFIER_PATTERN.matcher(javaExpression.substring(position));
        if (!matcher.matches()) {
            throw new JavaTokenParseException("No identifier found");
        }
        String identifier = matcher.group(1);
        // TODO: Only correct if no white spaces
        int length = identifier.length();
        boolean containsCaret = moveForward(length);
        return new JavaToken(identifier, containsCaret);
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
