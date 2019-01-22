package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.debug.ParserLogEntry;
import com.AMS.jBEAM.javaParser.debug.ParserLoggerIF;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.util.List;

public abstract class AbstractEntityParser
{
	final ParserContext				parserContext;
	final ObjectInfo				thisInfo;
	private final ParserLoggerIF	logger;

	AbstractEntityParser(ParserContext parserContext, ObjectInfo thisInfo) {
		this.parserContext = parserContext;
		this.thisInfo = thisInfo;
		logger = parserContext.getSettings().getLogger();
	}

	abstract ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes);

	public ParseResultIF parse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes) {
		logger.beginChildScope();
		log(LogLevel.INFO, "parsing at " + tokenStream);
		try {
			ParseResultIF parseResult = doParse(tokenStream.clone(), currentContextInfo, expectedResultTypes);
			log(LogLevel.INFO, "parse result: " + parseResult.getResultType());
			return parseResult;
		} finally {
			logger.endChildScope();
		}
	}

	void log(LogLevel logLevel, String message) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		final String suffix;
		if (stackTraceElements.length > 2) {
			StackTraceElement element = stackTraceElements[2];
			suffix = " (" + element.getClassName() + "." + element.getMethodName() + ":" + element.getLineNumber() + ")";
		} else {
			suffix = "";
		}
		logger.log(new ParserLogEntry(logLevel, getClass().getSimpleName(), message + suffix));
	}
}
