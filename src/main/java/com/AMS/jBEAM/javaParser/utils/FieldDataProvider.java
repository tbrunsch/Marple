package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionField;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionIF;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public class FieldDataProvider
{
	private final ParserContext parserContext;

	public FieldDataProvider(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	public CompletionSuggestions suggestFields(ObjectInfo contextInfo, List<TypeToken<?>> expectedTypes, final int insertionBegin, final int insertionEnd, boolean staticOnly) {
		TypeToken<?> contextType = parserContext.getObjectInfoProvider().getType(contextInfo);
		List<FieldInfo> fieldInfos = parserContext.getInspectionDataProvider().getFieldInfos(contextType, staticOnly);
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			fieldInfos,
			fieldInfo -> new CompletionSuggestionField(fieldInfo, insertionBegin, insertionEnd),
			ParseUtils.rateFieldByTypesFunc(contextInfo.getObject(), parserContext.getSettings().getEvaluationModeCodeCompletion(), expectedTypes)
		);
		return new CompletionSuggestions(ratedSuggestions);
	}
}
