package dd.kms.marple.api.settings.keys;

public interface KeySettingsBuilder
{
	static KeySettingsBuilder create() {
		return new dd.kms.marple.impl.settings.keys.KeySettingsBuilderImpl();
	}

	KeySettingsBuilder inspectionKey(KeyRepresentation inspectionKey);
	KeySettingsBuilder evaluationKey(KeyRepresentation evaluationKey);
	KeySettingsBuilder findInstancesKey(KeyRepresentation findInstancesKey);
	KeySettingsBuilder debugSupportKey(KeyRepresentation debugKey);
	KeySettingsBuilder customActionsKey(KeyRepresentation customActionKey);
	KeySettingsBuilder codeCompletionKey(KeyRepresentation codeCompletionKey);
	KeySettingsBuilder showMethodArgumentsKey(KeyRepresentation showMethodArgumentsKey);
	KeySettingsBuilder quickHelpKey(KeyRepresentation quickHelpKey);
	KeySettings build();
}
