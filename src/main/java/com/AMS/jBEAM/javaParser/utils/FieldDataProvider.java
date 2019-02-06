package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionField;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionIF;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class FieldDataProvider
{
	private final ParserContext parserContext;

	public FieldDataProvider(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	public CompletionSuggestions suggestFields(String expectedName, ObjectInfo contextInfo, List<TypeToken<?>> expectedTypes, int insertionBegin, int insertionEnd, boolean staticOnly) {
		TypeToken<?> contextType = parserContext.getObjectInfoProvider().getType(contextInfo);
		List<FieldInfo> fieldInfos = parserContext.getInspectionDataProvider().getFieldInfos(contextType, staticOnly);
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			fieldInfos,
			fieldInfo -> new CompletionSuggestionField(fieldInfo, insertionBegin, insertionEnd),
			rateFieldByNameAndTypesFunc(expectedName, contextInfo.getObject(), parserContext.getSettings().getEvaluationModeCodeCompletion(), expectedTypes)
		);
		return new CompletionSuggestions(ratedSuggestions);
	}

	private static int rateFieldByName(FieldInfo fieldInfo, String expectedName) {
		return ParseUtils.rateStringMatch(fieldInfo.getName(), expectedName);
	}

	private static int rateFieldByTypes(FieldInfo fieldInfo, Object instance, EvaluationMode evaluationMode, List<TypeToken<?>> expectedTypes) {
		if (expectedTypes == null) {
			return ParseUtils.TYPE_MATCH_FULL;
		}
		if (expectedTypes.isEmpty()) {
			return ParseUtils.TYPE_MATCH_NONE;
		}
		try {
			TypeToken<?> type = ObjectInfoProvider.getType(fieldInfo.get(instance), fieldInfo.getType(), evaluationMode);
			return	expectedTypes.stream().mapToInt(expectedType -> ParseUtils.rateTypeMatch(type, expectedType)).min().getAsInt();
		} catch (IllegalAccessException e) {
			// TODO: Swallowed exception
			return ParseUtils.TYPE_MATCH_NONE;
		}
	}

	private static ToIntFunction<FieldInfo> rateFieldByNameAndTypesFunc(String fieldName, Object instance, EvaluationMode evaluationMode, List<TypeToken<?>> expectedTypes) {
		return fieldInfo -> (ParseUtils.TYPE_MATCH_NONE + 1)*rateFieldByName(fieldInfo, fieldName) + rateFieldByTypes(fieldInfo, instance, evaluationMode, expectedTypes);
	}

	public static String getFieldDisplayText(FieldInfo fieldInfo) {
		return fieldInfo.getName() + " (" + fieldInfo.getDeclaringType() + ")";
	}
}
