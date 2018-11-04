package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractJavaEntityParser
{
    final JavaParserSettings	parserSettings;
    final Class<?>           	thisContextClass;

    AbstractJavaEntityParser(JavaParserSettings parserSettings, Class<?> thisContextClass) {
        this.parserSettings = parserSettings;
        this.thisContextClass = thisContextClass;
    }

    abstract ParseResultIF doParse(JavaTokenStream tokenStream, Class<?> currentContextClass, Class<?> expectedResultClass);

    ParseResultIF parse(final JavaTokenStream tokenStream, final Class<?> currentContextClass, final Class<?> expectedResultClass) {
        return doParse(tokenStream.clone(), currentContextClass, expectedResultClass);
    }

    CompletionSuggestions suggestFieldsAndMethods(Class<?> contextClass, Class<?> expectedResultClass, final int insertionBegin, final int insertionEnd) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = new LinkedHashMap<>();

		List<Field> fields = parserSettings.getInspectionDataProvider().getFields(contextClass, false);
		ratedSuggestions.putAll(ParseUtils.createRatedSuggestions(
				fields,
				field -> new CompletionSuggestionField(field, insertionBegin, insertionEnd),
				ParseUtils.rateFieldByClassFunc(expectedResultClass))
		);

		List<Method> methods = parserSettings.getInspectionDataProvider().getMethods(contextClass, false);
		ratedSuggestions.putAll(ParseUtils.createRatedSuggestions(
				methods,
				method -> new CompletionSuggestionMethod(method, insertionBegin, insertionEnd),
				ParseUtils.rateMethodByClassFunc(expectedResultClass))
		);

		return new CompletionSuggestions(ratedSuggestions);
	}
}
