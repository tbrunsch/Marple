package dd.kms.marple.settings.keys;

public interface KeySettingsBuilder
{
	KeySettingsBuilder inspectionKey(KeyRepresentation inspectionKey);
	KeySettingsBuilder evaluationKey(KeyRepresentation evaluationKey);
	KeySettingsBuilder searchKey(KeyRepresentation searchKey);
	KeySettingsBuilder debugSupportKey(KeyRepresentation debugKey);
	KeySettingsBuilder codeCompletionKey(KeyRepresentation codeCompletionKey);
	KeySettingsBuilder showMethodArgumentsKey(KeyRepresentation showMethodArgumentsKey);
	KeySettings build();
}
