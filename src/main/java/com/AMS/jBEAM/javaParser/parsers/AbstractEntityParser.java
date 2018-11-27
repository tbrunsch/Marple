package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.JavaParserContext;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;

import java.util.List;

public abstract class AbstractEntityParser
{
	final JavaParserContext parserContext;
	final ObjectInfo		thisInfo;

	AbstractEntityParser(JavaParserContext parserContext, ObjectInfo thisInfo) {
		this.parserContext = parserContext;
		this.thisInfo = thisInfo;
	}

	abstract ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses);

	public ParseResultIF parse(final TokenStream tokenStream, ObjectInfo currentContextInfo, final List<Class<?>> expectedResultClasses) {
		return doParse(tokenStream.clone(), currentContextInfo, expectedResultClasses);
	}
}
