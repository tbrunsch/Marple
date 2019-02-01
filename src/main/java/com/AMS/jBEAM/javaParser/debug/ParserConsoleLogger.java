package com.AMS.jBEAM.javaParser.debug;

import com.google.common.base.Strings;

public class ParserConsoleLogger extends AbstractParserLogger
{
	private static final int INDENT_SIZE	= 2;

	private boolean	printNumLoggedEntries	= false;

	@Override
	void doLog(ParserLogEntry entry, int indent) {
		StringBuilder builder = new StringBuilder();
		if (printNumLoggedEntries) {
			builder.append(Strings.padStart(String.valueOf(getNumberOfLoggedEntries()), 5, ' ')).append(" ");
		}
		for (int i = 0; i < indent * INDENT_SIZE; i++) {
			builder.append(" ");
		}
		System.out.println(formatLogEntry(entry, builder));
	}

	public ParserConsoleLogger printNumberOfLoggedEntries(boolean print) {
		this.printNumLoggedEntries = print;
		return this;
	}

	@Override
	void doStop() {
		// set a break point here to stop at the desired point in time
		System.out.println("Stopping after " + getNumberOfLoggedEntries() + " entries");
	}

	private String formatLogEntry(ParserLogEntry entry, StringBuilder builder) {
		builder.append(entry.getContext()).append(": ").append(entry.getLogLevel().getPrefix()).append(entry.getMessage());
		return builder.toString();
	}
}
