package com.AMS.jBEAM.javaParser.debug;

public class ParserConsoleLogger implements ParserLoggerIF
{
	private static final int 	SCOPE_INDENT_SIZE	= 2;

	private int	indentSize					= 0;
	private int numLoggedEntries			= 0;
	private int	getNumLoggedEntriesToStopAt	= -1;

	@Override
	public void beginChildScope() {
		indentSize += SCOPE_INDENT_SIZE;
	}

	@Override
	public void endChildScope() {
		indentSize -= SCOPE_INDENT_SIZE;
	}

	@Override
	public void log(ParserLogEntry entry) {
		numLoggedEntries++;

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < indentSize; i++) {
			builder.append(" ");
		}
		builder.append(formatLogEntry(entry));
		System.out.println(builder.toString());

		if (numLoggedEntries == getNumLoggedEntriesToStopAt) {
			stop();
		}
	}

	@Override
	public int getNumberOfLoggedEntries() {
		return numLoggedEntries;
	}

	public ParserConsoleLogger stopAt(int getNumLoggedEntriesToStopAt) {
		this.getNumLoggedEntriesToStopAt = getNumLoggedEntriesToStopAt;
		return this;
	}

	private void stop() {
		// set a break point here to stop at the desired point in time
		System.out.println("Stopping after " + getNumLoggedEntriesToStopAt + " entries");
	}

	private static String formatLogEntry(ParserLogEntry entry) {
		return entry.getContext() + ": " + entry.getLogLevel().getPrefix() + entry.getMessage();
	}
}
