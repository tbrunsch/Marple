package dd.kms.marple.impl.aiagent;

final class TranscriptEntry
{
	enum Role
	{
		USER,
		AGENT,
		PROGRESS,
		TOOL,
		INFO,
		ERROR
	}

	private final Role		role;
	private final String	text;

	TranscriptEntry(Role role, String text) {
		this.role = role;
		this.text = text;
	}

	Role getRole() {
		return role;
	}

	String getText() {
		return text;
	}
}
