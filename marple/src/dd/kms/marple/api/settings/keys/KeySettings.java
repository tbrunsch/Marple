package dd.kms.marple.api.settings.keys;

public interface KeySettings
{
	KeyRepresentation getInspectionKey();
	KeyRepresentation getEvaluationKey();
	KeyRepresentation getFindInstancesKey();
	KeyRepresentation getDebugSupportKey();
	KeyRepresentation getTriggerBreakpointKey();
	KeyRepresentation getCodeCompletionKey();
	KeyRepresentation getShowMethodArgumentsKey();
	KeyRepresentation getQuickHelpKey();
}
