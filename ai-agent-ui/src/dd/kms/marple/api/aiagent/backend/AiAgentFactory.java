package dd.kms.marple.api.aiagent.backend;

import javax.annotation.Nullable;

/**
 * Creates the agents for the "AI Agent" view (see {@link AiAgentFactorySupplier}).
 */
public interface AiAgentFactory
{
	/**
	 * Creates a new agent. This method is called on the event dispatch thread and must not block for a long
	 * time: The agent must be started lazily on the first request (see {@link AiAgent#sendRequest(String, AiAgentListener)}),
	 * which is sent outside the event dispatch thread.
	 *
	 * @param resumeState	the resume state of a previous agent (see {@link AiAgent#getResumeState()})
	 *                      or {@code null} if a new conversation should be started
	 */
	AiAgent createAgent(AiAgentEnvironment environment, @Nullable Object resumeState);

	/**
	 * Returns {@code true} if the agents can create and modify files, typically the source code of the inspected
	 * application. Otherwise, the view only offers the scope "Running application", in which the agents
	 * analyze and change the running application only.
	 */
	boolean supportsFileChanges();
}
