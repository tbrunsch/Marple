package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.JavaParserContext;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.JavaTokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;

import java.util.List;

public abstract class AbstractJavaEntityParser
{
	final JavaParserContext parserContext;
	final ObjectInfo		thisInfo;

	AbstractJavaEntityParser(JavaParserContext parserContext, ObjectInfo thisInfo) {
		this.parserContext = parserContext;
		this.thisInfo = thisInfo;
	}

	abstract ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses);

	public ParseResultIF parse(final JavaTokenStream tokenStream, ObjectInfo currentContextInfo, final List<Class<?>> expectedResultClasses) {
		return doParse(tokenStream.clone(), currentContextInfo, expectedResultClasses);
	}
}
