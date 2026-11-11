package dd.kms.marple.api.aiagent.backend;

public final class McpEndpoint
{
	private final String	url;
	private final String	bearerToken;

	public McpEndpoint(String url, String bearerToken) {
		this.url = url;
		this.bearerToken = bearerToken;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * Every request to the MCP server must contain the header {@code Authorization: Bearer <token>}.
	 */
	public String getBearerToken() {
		return bearerToken;
	}
}
