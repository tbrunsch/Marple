package dd.kms.marple.settings.keys;

public interface KeySettingsBuilder
{
	KeySettingsBuilder inspectionKey(KeyRepresentation inspectionKey);
	KeySettingsBuilder evaluationKey(KeyRepresentation evaluationKey);
	KeySettingsBuilder findInstancesKey(KeyRepresentation findInstancesKey);
	KeySettingsBuilder debugSupportKey(KeyRepresentation debugKey);
	KeySettingsBuilder codeCompletionKey(KeyRepresentation codeCompletionKey);
	KeySettingsBuilder showMethodArgumentsKey(KeyRepresentation showMethodArgumentsKey);
	KeySettingsBuilder quickHelpKey(KeyRepresentation quickHelpKey);
	KeySettings build();
}
