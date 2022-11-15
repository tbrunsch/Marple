package dd.kms.marple.api.settings.evaluation;

public interface EvaluationSettingsBuilder
{
	static EvaluationSettingsBuilder create() {
		return new dd.kms.marple.impl.settings.evaluation.EvaluationSettingsBuilderImpl();
	}

	EvaluationSettingsBuilder suggestExpressionToEvaluate(Class<?> objectClass, String expression);
	EvaluationSettings build();
}
