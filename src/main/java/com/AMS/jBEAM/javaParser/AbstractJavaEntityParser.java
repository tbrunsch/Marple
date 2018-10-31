package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

    CompletionSuggestions suggestFieldsAndMethods(Class<?> contextClass, Class<?> expectedResultClass, int insertionBegin, int insertionEnd) {
		List<CompletionSuggestion> suggestions = new ArrayList<>();

		List<Field> fields = parserSettings.getInspectionDataProvider().getFields(contextClass, false);
		suggestions.addAll(ParseUtils.createSuggestions(
				fields,
				ParseUtils.fieldTextInsertionInfoFunction(insertionBegin, insertionEnd),
				ParseUtils.FIELD_DISPLAY_FUNC,
				ParseUtils.rateFieldByClassFunc(expectedResultClass))
		);

		List<Method> methods = parserSettings.getInspectionDataProvider().getMethods(contextClass, false);
		suggestions.addAll(ParseUtils.createSuggestions(
				methods,
				ParseUtils.methodTextInsertionInfoFunction(insertionBegin, insertionEnd),
				ParseUtils.METHOD_DISPLAY_FUNC,
				ParseUtils.rateMethodByClassFunc(expectedResultClass))
		);

		return new CompletionSuggestions(suggestions);
	}
}
