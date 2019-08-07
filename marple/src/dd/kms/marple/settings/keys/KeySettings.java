package dd.kms.marple.settings.keys;

public interface KeySettings
{
	KeyRepresentation getInspectionKey();
	KeyRepresentation getEvaluationKey();
	KeyRepresentation getFindInstancesKey();
	KeyRepresentation getDebugSupportKey();
	KeyRepresentation getCodeCompletionKey();
	KeyRepresentation getShowMethodArgumentsKey();
	KeyRepresentation getQuickHelpKey();
}
