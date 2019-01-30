package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.debug.ParserConsoleLogger;
import com.AMS.jBEAM.javaParser.debug.ParserLoggerIF;
import com.AMS.jBEAM.javaParser.debug.ParserNullLogger;

class AbstractTestExecutor<T extends AbstractTestExecutor>
{
	final Object			testInstance;

	boolean					stopAtError				= false;
	boolean					printLogEntriesAtError	= false;

	ParserSettingsBuilder	settingsBuilder			= new ParserSettingsBuilder().minimumAccessLevel(AccessLevel.PRIVATE);

	AbstractTestExecutor(Object testInstance) {
		this.testInstance = testInstance;
	}

	T addVariable(Variable variable) {
		settingsBuilder.addVariable(variable);
		return (T) this;
	}

	T minimumAccessLevel(AccessLevel minimumAccessLevel) {
		settingsBuilder.minimumAccessLevel(minimumAccessLevel);
		return (T) this;
	}

	T stopAtError() {
		stopAtError = true;
		return (T) this;
	}

	T printLogEntriesAtError() {
		printLogEntriesAtError = true;
		return (T) this;
	}

	ParserLoggerIF prepareLogger(boolean printToConsole, int numLoggedEntriesToStopAfter) {
		ParserLoggerIF logger = printToConsole
									? new ParserConsoleLogger().printNumberOfLoggedEntries(true)
									: new ParserNullLogger();
		logger.stopAfter(numLoggedEntriesToStopAfter);
		settingsBuilder.logger(logger);
		return logger;
	}
}