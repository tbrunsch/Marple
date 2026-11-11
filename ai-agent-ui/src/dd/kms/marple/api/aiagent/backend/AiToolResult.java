package dd.kms.marple.api.aiagent.backend;

public final class AiToolResult
{
	public static AiToolResult success(String text) {
		return new AiToolResult(text, false);
	}

	public static AiToolResult error(String text) {
		return new AiToolResult(text, true);
	}

	private final String	text;
	private final boolean	error;

	private AiToolResult(String text, boolean error) {
		this.text = text;
		this.error = error;
	}

	public String getText() {
		return text;
	}

	public boolean isError() {
		return error;
	}
}
