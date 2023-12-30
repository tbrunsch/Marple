package dd.kms.marple.api.settings.evaluation;

import java.util.Collection;
import java.util.Map;

public interface EvaluationSettings
{
	String suggestExpressionToEvaluate(Object thisValue);
	Collection<NamedObject> getRelatedObjects(Object object);
	Map<String, AdditionalEvaluationSettings> getAdditionalSettings();
}
