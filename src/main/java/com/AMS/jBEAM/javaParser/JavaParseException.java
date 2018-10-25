package com.AMS.jBEAM.javaParser;

import java.text.MessageFormat;

public class JavaParseException extends Exception
{
    JavaParseException(ParseError parseError) {
        super(MessageFormat.format("Parse exception at position {0}: {1}", parseError.getPosition(), parseError.getMessage()));
    }
}
