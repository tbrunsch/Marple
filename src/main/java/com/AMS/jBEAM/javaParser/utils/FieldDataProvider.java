package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionField;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionIF;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class FieldDataProvider
{
	private final ParserContext parserContext;

	public FieldDataProvider(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	public CompletionSuggestions suggestFields(ObjectInfo contextInfo, List<Class<?>> expectedClasses, final int insertionBegin, final int insertionEnd, boolean staticOnly) {
		Class<?> contextClass = parserContext.getObjectInfoProvider().getClass(contextInfo);
		List<Field> fields = parserContext.getInspectionDataProvider().getFields(contextClass, staticOnly);
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			fields,
			field -> new CompletionSuggestionField(field, insertionBegin, insertionEnd),
			ParseUtils.rateFieldByClassesFunc(expectedClasses)
		);
		return new CompletionSuggestions(ratedSuggestions);
	}
}
