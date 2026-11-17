package dd.kms.marple.api.aiagent.backend;

import javax.annotation.Nullable;

/**
 * Creates the agents for the "AI Agent" view (see {@link AiAgentFactorySupplier}).
 */
public interface AiAgentFactory
{
	/**
	 * Creates a new agent. The agent should be started lazily on the first request.
	 *
	 * @param resumeState	the resume state of a previous agent (see {@link AiAgent#getResumeState()})
	 *                      or {@code null} if a new conversation should be started
	 */
	AiAgent createAgent(AiAgentEnvironment environment, @Nullable Object resumeState);
}
