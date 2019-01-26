package com.AMS.jBEAM.javaParser.debug;

import com.google.common.base.Strings;

public class ParserConsoleLogger implements ParserLoggerIF
{
	private static final int 	SCOPE_INDENT_SIZE	= 2;

	private int		indentSize					= 0;
	private int		numLoggedEntries			= 0;
	private int		getNumLoggedEntriesToStopAt	= -1;
	private boolean	printNumLoggedEntries		= false;

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
		if (printNumLoggedEntries) {
			builder.append(Strings.padStart(String.valueOf(numLoggedEntries), 5, ' ')).append(" ");
		}
		for (int i = 0; i < indentSize; i++) {
			builder.append(" ");
		}
		builder.append(formatLogEntry(entry, builder));
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

	public ParserConsoleLogger printNumberOfLoggedEntries(boolean print) {
		this.printNumLoggedEntries = print;
		return this;
	}

	private void stop() {
		// set a break point here to stop at the desired point in time
		System.out.println("Stopping after " + getNumLoggedEntriesToStopAt + " entries");
	}

	private String formatLogEntry(ParserLogEntry entry, StringBuilder builder) {
		builder.append(entry.getContext()).append(": ").append(entry.getLogLevel().getPrefix()).append(entry.getMessage());
		return builder.toString();
	}
}
