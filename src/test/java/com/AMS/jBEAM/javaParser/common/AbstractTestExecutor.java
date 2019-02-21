package com.AMS.jBEAM.javaParser.common;

import com.AMS.jBEAM.javaParser.debug.ParserConsoleLogger;
import com.AMS.jBEAM.javaParser.debug.ParserLoggerIF;
import com.AMS.jBEAM.javaParser.debug.ParserNullLogger;
import com.AMS.jBEAM.javaParser.settings.AccessLevel;
import com.AMS.jBEAM.javaParser.settings.ObjectTreeNodeIF;
import com.AMS.jBEAM.javaParser.settings.ParserSettingsBuilder;
import com.AMS.jBEAM.javaParser.settings.Variable;

public class AbstractTestExecutor<T extends AbstractTestExecutor>
{
	protected final Object					testInstance;
	protected final ParserSettingsBuilder	settingsBuilder			= new ParserSettingsBuilder().minimumAccessLevel(AccessLevel.PRIVATE);


	private boolean							stopAtError				= false;
	private boolean							printLogEntriesAtError	= false;

	public AbstractTestExecutor(Object testInstance) {
		this.testInstance = testInstance;
	}

	public T addVariable(Variable variable) {
		settingsBuilder.addVariable(variable);
		return (T) this;
	}

	public T minimumAccessLevel(AccessLevel minimumAccessLevel) {
		settingsBuilder.minimumAccessLevel(minimumAccessLevel);
		return (T) this;
	}

	public T importClass(String className) {
		settingsBuilder.importClass(className);
		return (T) this;
	}

	public T importPackage(String packageName) {
		settingsBuilder.importPackage(packageName);
		return (T) this;
	}

	public T customHierarchyRoot(ObjectTreeNodeIF root) {
		settingsBuilder.customHierarchyRoot(root);
		return (T) this;
	}

	public T stopAtError() {
		stopAtError = true;
		return (T) this;
	}

	public T printLogEntriesAtError() {
		printLogEntriesAtError = true;
		return (T) this;
	}

	protected ParserLoggerIF prepareLogger(boolean printToConsole, int numLoggedEntriesToStopAfter) {
		ParserLoggerIF logger = printToConsole
									? new ParserConsoleLogger().printNumberOfLoggedEntries(true)
									: new ParserNullLogger();
		logger.stopAfter(numLoggedEntriesToStopAfter);
		settingsBuilder.logger(logger);
		return logger;
	}

	protected boolean isStopAtError() {
		return stopAtError;
	}

	protected boolean isPrintLogEntriesAtError() {
		return printLogEntriesAtError;
	}
}
