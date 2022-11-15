package dd.kms.marple.api.settings.evaluation;

import java.util.Collection;

public interface EvaluationSettings
{
	String suggestExpressionToEvaluate(Object thisValue);
	Collection<NamedObject> getRelatedObjects(Object object);
}
