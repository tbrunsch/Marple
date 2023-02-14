package dd.kms.marple.api.settings.keys;

public interface KeySettings
{
	KeyRepresentation getInspectionKey();
	KeyRepresentation getEvaluationKey();
	KeyRepresentation getFindInstancesKey();
	KeyRepresentation getDebugSupportKey();
	KeyRepresentation getCustomActionsKey();
	KeyRepresentation getCodeCompletionKey();
	KeyRepresentation getShowMethodArgumentsKey();
	KeyRepresentation getQuickHelpKey();
}
