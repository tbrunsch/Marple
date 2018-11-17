package com.AMS.jBEAM.javaParser;

import java.text.MessageFormat;

public class JavaParseException extends Exception
{
	JavaParseException(int position, String message) {
		super(MessageFormat.format("Parse exception at position {0}: {1}", position, message));
	}
}
