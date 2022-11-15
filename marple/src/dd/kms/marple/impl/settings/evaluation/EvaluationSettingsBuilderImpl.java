package dd.kms.marple.impl.settings.evaluation;

import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;

import java.util.HashMap;
import java.util.Map;

public class EvaluationSettingsBuilderImpl implements EvaluationSettingsBuilder
{
	/*
	 * Since we want the user to add expressions without having to know for which classes expressions
	 * have already been registered, we cannot use ImmutableMap.Builder here. Otherwise, the user would
	 * get an exception if he specified an expression for the same object class for which there is already
	 * a default expression defined.
	 */
	private final Map<Class<?>, String> suggestedExpressions	= new HashMap<>();

	@Override
	public EvaluationSettingsBuilder suggestExpressionToEvaluate(Class<?> objectClass, String expression) {
		suggestedExpressions.put(objectClass, expression);
		return this;
	}

	@Override
	public EvaluationSettings build() {
		return new EvaluationSettingsImpl(suggestedExpressions);
	}
}
