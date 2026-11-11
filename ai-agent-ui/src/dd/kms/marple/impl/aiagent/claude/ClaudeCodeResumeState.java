package dd.kms.marple.impl.aiagent.claude;

/**
 * Claude Code persists sessions on disk. A session can be continued via {@code --resume <session id>}.
 */
final class ClaudeCodeResumeState
{
	private final String	sessionId;

	ClaudeCodeResumeState(String sessionId) {
		this.sessionId = sessionId;
	}

	String getSessionId() {
		return sessionId;
	}

	@Override
	public String toString() {
		return "Claude Code session " + sessionId;
	}
}
