package com.AMS.jBEAM.javaParser.debug;

public class ParserLogEntry
{
	private final LogLevel	logLevel;
	private final String	context;
	private final String	message;

	public ParserLogEntry(LogLevel logLevel, String context, String message) {
		this.logLevel = logLevel;
		this.context = context;
		this.message = message;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public String getContext() {
		return context;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return logLevel + ": " + context + ": " + message;
	}
}
