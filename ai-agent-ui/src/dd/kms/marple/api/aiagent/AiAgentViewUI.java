package dd.kms.marple.api.aiagent;

import dd.kms.marple.api.aiagent.backend.AiAgentFactorySupplier;
import dd.kms.marple.api.settings.visual.VisualSettingsBuilder;

/**
 * Registration helper for the "AI Agent" inspection view. The view lets the user chat with an AI agent
 * about the inspected object. The agent can query and manipulate the runtime state via tools
 * provided by an MCP server that runs inside the inspected JVM.<br>
 * <br>
 * Example:
 * <pre>
 * AiAgentViewUI.create(() -&gt; {
 *         Path sourceRootDirectory = findSourceRootDirectory();
 *         if (sourceRootDirectory == null) {
 *             throw new IOException("Cannot find the source code of the application");
 *         }
 *         return ClaudeCodeAgentFactoryBuilder.create(sourceRootDirectory).build();
 *     })
 *     .systemPromptAddition("The inspected application is ...")
 *     .register(visualSettingsBuilder);
 * </pre>
 */
public interface AiAgentViewUI
{
	/**
	 * @param agentFactorySupplier	provides the factory that creates the agents. If it throws an exception,
	 *                              then the view displays the exception's message instead of the chat
	 *                              (see {@link AiAgentFactorySupplier}). If the factory can always be
	 *                              created, use {@code create(() -> agentFactory)}.
	 */
	static AiAgentViewUI create(AiAgentFactorySupplier agentFactorySupplier) {
		return new dd.kms.marple.impl.aiagent.AiAgentViewUIImpl(agentFactorySupplier);
	}

	/**
	 * Application specific information that is appended to the system prompt.
	 */
	AiAgentViewUI systemPromptAddition(String systemPromptAddition);

	VisualSettingsBuilder register(VisualSettingsBuilder visualSettingsBuilder);
}
