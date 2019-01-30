package com.AMS.jBEAM.javaParser.debug;

public interface ParserLoggerIF
{
	void beginChildScope();
	void endChildScope();
	void log(ParserLogEntry entry);

	int getNumberOfLoggedEntries();
	void stopAfter(int numLoggedEntries);
}
