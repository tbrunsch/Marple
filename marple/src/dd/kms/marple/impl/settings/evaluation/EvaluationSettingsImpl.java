package dd.kms.marple.impl.settings.evaluation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.settings.evaluation.AdditionalEvaluationSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.evaluation.NamedObject;
import dd.kms.marple.impl.common.ReflectionUtils;

import java.util.*;
import java.util.function.Function;

class EvaluationSettingsImpl implements EvaluationSettings
{
	private final Map<Class<?>, String>										suggestedExpressions;
	private final List<Function<Object, ? extends Collection<NamedObject>>>	relatedObjectProviders;
	private final Map<String, AdditionalEvaluationSettings>					additionalSettings;

	EvaluationSettingsImpl(Map<Class<?>, String> suggestedExpressions, List<Function<Object, ? extends Collection<NamedObject>>> relatedObjectProviders, Map<String, AdditionalEvaluationSettings> additionalSettings) {
		this.suggestedExpressions = ImmutableMap.copyOf(suggestedExpressions);
		this.relatedObjectProviders = ImmutableList.copyOf(relatedObjectProviders);
		this.additionalSettings = ImmutableMap.copyOf(additionalSettings);
	}

	@Override
	public String suggestExpressionToEvaluate(Object thisValue) {
		if (thisValue == null) {
			return "this";
		}
		Class<?> thisClass = ReflectionUtils.getBestMatchingClass(thisValue, suggestedExpressions.keySet());
		return thisClass == null ? "this" : suggestedExpressions.get(thisClass);
	}

	@Override
	public Collection<NamedObject> getRelatedObjects(Object object) {
		if (object == null) {
			return Collections.emptyList();
		}
		Set<NamedObject> allRelatedObjects = null;
		for (Function<Object, ? extends Collection<NamedObject>> relatedObjectProvider : relatedObjectProviders) {
			Collection<NamedObject> relatedObjects = relatedObjectProvider.apply(object);
			if (relatedObjects.isEmpty()) {
				continue;
			}
			if (allRelatedObjects == null) {
				allRelatedObjects = new LinkedHashSet<>();
			}
			allRelatedObjects.addAll(relatedObjects);
		}
		return allRelatedObjects != null ? allRelatedObjects : Collections.emptyList();
	}

	@Override
	public Map<String, AdditionalEvaluationSettings> getAdditionalSettings() {
		return additionalSettings;
	}
}
