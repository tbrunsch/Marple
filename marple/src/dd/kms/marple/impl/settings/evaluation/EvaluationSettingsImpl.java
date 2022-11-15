package dd.kms.marple.impl.settings.evaluation;

import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.impl.common.ReflectionUtils;

import java.util.Map;

class EvaluationSettingsImpl implements EvaluationSettings
{
	private final Map<Class<?>, String>	suggestedExpressions;

	EvaluationSettingsImpl(Map<Class<?>, String> suggestedExpressions) {
		this.suggestedExpressions = ImmutableMap.copyOf(suggestedExpressions);
	}

	@Override
	public String suggestExpressionToEvaluate(Object thisValue) {
		if (thisValue == null) {
			return "this";
		}
		Class<?> thisClass = ReflectionUtils.getBestMatchingClass(thisValue, suggestedExpressions.keySet());
		return thisClass == null ? "this" : suggestedExpressions.get(thisClass);
	}
}
