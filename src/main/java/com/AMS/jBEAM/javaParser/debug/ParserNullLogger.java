package com.AMS.jBEAM.javaParser.debug;

public class ParserNullLogger implements ParserLoggerIF
{
	@Override
	public void beginChildScope() {}

	@Override
	public void endChildScope() {}

	@Override
	public void log(ParserLogEntry entry) {}

	@Override
	public int getNumberOfLoggedEntries() {
		return 0;
	}
}
