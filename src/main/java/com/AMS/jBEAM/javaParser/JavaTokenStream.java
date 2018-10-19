package com.AMS.jBEAM.javaParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JavaTokenStream implements Cloneable
{
    // TODO: Support white spaces
    private static final Pattern    IDENTIFIER_PATTERN  = Pattern.compile("^([A-Za-z][_A-Za-z0-9]*).*");

    private final String    javaExpression;
    private final int       carret;

    private int             position;

    JavaTokenStream(String javaExpression, int carret) {
        this(javaExpression, carret, 0);
    }

    private JavaTokenStream(String javaExpression, int carret, int position) {
        this.javaExpression = javaExpression;
        this.carret = carret;
        this.position = position;
    }

    boolean hasMore() {
        return position < javaExpression.length();
    }

    JavaToken readIdentifier() throws JavaTokenParseException {
        checkHasMore();
        Matcher matcher = IDENTIFIER_PATTERN.matcher(javaExpression.substring(position));
        if (!matcher.matches()) {
            throw new JavaTokenParseException("No identifier found", position);
        }
        String identifier = matcher.group(1);
        boolean containsCarret = moveToNextPosition(position + identifier.length());
        return new JavaToken(identifier, containsCarret);
    }

    JavaToken readDot() throws JavaTokenParseException {
        checkHasMore();
        if (javaExpression.charAt(position) == '.') {
            boolean containsCarret = moveToNextPosition(position + 1);
            return new JavaToken(".", containsCarret);
        }
        throw new JavaTokenParseException("Dot not found", position);
    }

    private void checkHasMore() throws JavaTokenParseException {
        if (!hasMore()) {
            throw new JavaTokenParseException("End of Java expression reached", position);
        }
    }

    /**
     * Returns true whether the position strived the carret when moving from position to nextPosition
     */
    private boolean moveToNextPosition(int nextPosition) {
        boolean containsCarret = position < carret && carret <= nextPosition;
        position = nextPosition;
        return containsCarret;
    }

    @Override
    public JavaTokenStream clone() {
        return new JavaTokenStream(javaExpression, carret, position);
    }

    static class JavaTokenParseException extends Exception
    {
        private final int       position;

        JavaTokenParseException(String message, int position) {
            super(message);
            this.position = position;
        }

        int getPosition() {
            return position;
        }
    }
}
