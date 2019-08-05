package dd.kms.marple.settings.keys;

public interface KeySettings
{
	KeyRepresentation getInspectionKey();
	KeyRepresentation getEvaluationKey();
	KeyRepresentation getSearchKey();
	KeyRepresentation getDebugSupportKey();
	KeyRepresentation getCodeCompletionKey();
	KeyRepresentation getShowMethodArgumentsKey();
}
