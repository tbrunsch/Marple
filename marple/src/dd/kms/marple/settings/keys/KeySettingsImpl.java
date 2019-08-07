package dd.kms.marple.settings.keys;

class KeySettingsImpl implements KeySettings
{
	private final KeyRepresentation	inspectionKey;
	private final KeyRepresentation	evaluationKey;
	private final KeyRepresentation findInstancesKey;
	private final KeyRepresentation	debugSupportKey;
	private final KeyRepresentation	codeCompletionKey;
	private final KeyRepresentation	showMethodArgumentsKey;

	KeySettingsImpl(KeyRepresentation inspectionKey, KeyRepresentation evaluationKey, KeyRepresentation findInstancesKey, KeyRepresentation debugSupportKey, KeyRepresentation codeCompletionKey, KeyRepresentation showMethodArgumentsKey) {
		this.inspectionKey = inspectionKey;
		this.evaluationKey = evaluationKey;
		this.findInstancesKey = findInstancesKey;
		this.debugSupportKey = debugSupportKey;
		this.codeCompletionKey = codeCompletionKey;
		this.showMethodArgumentsKey = showMethodArgumentsKey;
	}

	@Override
	public KeyRepresentation getInspectionKey() {
		return inspectionKey;
	}

	@Override
	public KeyRepresentation getEvaluationKey() {
		return evaluationKey;
	}

	@Override
	public KeyRepresentation getFindInstancesKey() {
		return findInstancesKey;
	}

	@Override
	public KeyRepresentation getDebugSupportKey() {
		return debugSupportKey;
	}

	@Override
	public KeyRepresentation getCodeCompletionKey() {
		return codeCompletionKey;
	}

	@Override
	public KeyRepresentation getShowMethodArgumentsKey() {
		return showMethodArgumentsKey;
	}
}
