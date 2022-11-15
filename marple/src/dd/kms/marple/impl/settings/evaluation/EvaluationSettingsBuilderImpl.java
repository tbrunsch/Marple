package dd.kms.marple.impl.settings.evaluation;

import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;
import dd.kms.marple.api.settings.evaluation.NamedObject;

import java.util.*;
import java.util.function.Function;

public class EvaluationSettingsBuilderImpl implements EvaluationSettingsBuilder
{
	private final Map<Class<?>, String> 									suggestedExpressions	= new HashMap<>();
	private final List<Function<Object, ? extends Collection<NamedObject>>>	relatedObjectProviders	= new ArrayList<>();

	@Override
	public EvaluationSettingsBuilder suggestExpressionToEvaluate(Class<?> objectClass, String expression) {
		suggestedExpressions.put(objectClass, expression);
		return this;
	}

	@Override
	public <T> EvaluationSettingsBuilder addRelatedObjectsProvider(Class<T> objectClass, Function<T, ? extends Collection<NamedObject>> relatedObjectProvider) {
		Function<Object, ? extends Collection<NamedObject>> genericRelatedObjectProvider = object -> objectClass.isInstance(object)
			? relatedObjectProvider.apply(objectClass.cast(object))
			: Collections.emptyList();
		relatedObjectProviders.add(genericRelatedObjectProvider);
		return this;
	}

	@Override
	public EvaluationSettings build() {
		return new EvaluationSettingsImpl(suggestedExpressions, relatedObjectProviders);
	}
}
