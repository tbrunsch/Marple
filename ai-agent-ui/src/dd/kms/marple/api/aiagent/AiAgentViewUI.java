package dd.kms.marple.api.aiagent;

import dd.kms.marple.api.aiagent.backend.AiAgentFactory;
import dd.kms.marple.api.settings.visual.VisualSettingsBuilder;

import javax.annotation.Nullable;

/**
 * Registration helper for the "AI Agent" inspection view. The view lets the user chat with an AI agent
 * about the inspected object. The agent can query and manipulate the runtime state via tools
 * provided by an MCP server that runs inside the inspected JVM.<br>
 * <br>
 * Example:
 * <pre>
 * AiAgentViewUI.create()
 *     .agentFactory(agentFactory)
 *     .systemPromptAddition("The inspected application is ...")
 *     .register(visualSettingsBuilder);
 * </pre>
 */
public interface AiAgentViewUI
{
	static AiAgentViewUI create() {
		return new dd.kms.marple.impl.aiagent.AiAgentViewUIImpl();
	}

	/**
	 * Specifies how the agents are created. If {@code null} is specified, then the view displays
	 * the "unavailable" message (see {@link #unavailableMessage(String)}).
	 */
	AiAgentViewUI agentFactory(@Nullable AiAgentFactory agentFactory);

	AiAgentViewUI unavailableMessage(String unavailableMessage);

	/**
	 * Application specific information that is appended to the system prompt.
	 */
	AiAgentViewUI systemPromptAddition(String systemPromptAddition);

	VisualSettingsBuilder register(VisualSettingsBuilder visualSettingsBuilder);
}
