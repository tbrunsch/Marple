package dd.kms.marple.api.aiagent.backend;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Agent-neutral interface of an AI agent. Except for {@link #getResumeState()}, all methods are
 * called outside the event dispatch thread.
 */
public interface AiAgent
{
	/**
	 * Sends a request to the agent and blocks until the agent has answered. The agent is started
	 * lazily on the first request. The listener is informed about the progress.
	 *
	 * @return the agent's final answer (Markdown)
	 */
	String sendRequest(String message, AiAgentListener listener) throws IOException;

	/**
	 * Cancels a running request. The method {@link #sendRequest(String, AiAgentListener)} then
	 * throws an exception. The agent must remain usable for further requests.
	 */
	void cancelRequest();

	/**
	 * Returns an opaque state that allows a new agent to continue the current conversation
	 * (see {@link AiAgentFactory#createAgent(AiAgentEnvironment, Object)}) or {@code null}
	 * if this is not supported or if there is no conversation yet. This method must be cheap.
	 */
	@Nullable
	Object getResumeState();

	/**
	 * Terminates the agent and releases all resources it holds (e.g., an agent process or temporary
	 * files). The agent cannot be used anymore afterwards.
	 * <ul>
	 *     <li>Must not throw exceptions. Failures while releasing resources should be ignored.</li>
	 *     <li>Must be idempotent: Calling it several times has the same effect as calling it once.</li>
	 *     <li>May be called while a request is running. Then {@link #sendRequest(String, AiAgentListener)}
	 *     must terminate with an exception.</li>
	 *     <li>Must not block for a long time. Graceful termination of external processes should be
	 *     done asynchronously.</li>
	 * </ul>
	 */
	void close();
}
