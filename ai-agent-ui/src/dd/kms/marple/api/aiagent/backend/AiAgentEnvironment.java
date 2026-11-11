package dd.kms.marple.api.aiagent.backend;


import java.util.List;

/**
 * Describes everything an agent needs to know about the Marple side.
 */
public interface AiAgentEnvironment
{
	/**
	 * The MCP server (Streamable HTTP transport) that provides the tools.
	 */
	McpEndpoint getMcpEndpoint();

	/**
	 * Name under which the MCP server should be registered at the agent.
	 */
	String getMcpServerName();

	/**
	 * The tools offered by the MCP server. Agents that do not support MCP can call them directly.
	 */
	List<AiTool> getTools();

	/**
	 * The system prompt the agent must pass to its model. Besides general instructions, it contains the
	 * rules for using the tools that the tool descriptions refer to (e.g., the syntax of Zenodot expressions),
	 * so the tools cannot be used reliably without it.
	 */
	String getSystemPrompt();

	/**
	 * The handler the agent must ask before performing actions that require approval, in particular before
	 * creating or modifying files. Agents must deny all other actions that are neither covered by the tools
	 * themselves nor explicitly allowed by the agent's configuration.
	 */
	PermissionHandler getPermissionHandler();

	/**
	 * Registers an agent-specific tool at the MCP server (e.g., a tool for permission prompts).
	 * Must be called before the agent connects to the MCP server.
	 */
	void registerAdditionalTool(AiTool tool);
}
