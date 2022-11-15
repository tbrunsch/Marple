package dd.kms.marple.api.settings.evaluation;

import java.util.Collection;
import java.util.function.Function;

public interface EvaluationSettingsBuilder
{
	static EvaluationSettingsBuilder create() {
		return new dd.kms.marple.impl.settings.evaluation.EvaluationSettingsBuilderImpl();
	}

	EvaluationSettingsBuilder suggestExpressionToEvaluate(Class<?> objectClass, String expression);

	<T> EvaluationSettingsBuilder addRelatedObjectsProvider(Class<T> objectClass, Function<T, ? extends Collection<NamedObject>> relatedObjectProvider);

	EvaluationSettings build();
}
