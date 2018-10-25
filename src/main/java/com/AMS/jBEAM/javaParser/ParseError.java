package com.AMS.jBEAM.javaParser;

class ParseError implements ParseResultIF
{
    private final int       position;
    private final String    message;

    ParseError(int position, String message) {
        this.position = position;
        this.message = message;
    }

    @Override
    public ParseResultType getResultType() {
        return ParseResultType.PARSE_ERROR;
    }

    int getPosition() {
        return position;
    }

    String getMessage() {
        return message;
    }
}
