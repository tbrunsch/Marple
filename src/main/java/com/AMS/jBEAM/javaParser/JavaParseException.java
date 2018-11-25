package com.AMS.jBEAM.javaParser;

import java.text.MessageFormat;

public class JavaParseException extends Exception
{
	JavaParseException(int position, String message) {
		this(position, message, null);
	}

	JavaParseException(int position, String message, Throwable cause) {
		super(MessageFormat.format("Parse exception at position {0}: {1}", position, message), cause);
	}
}
