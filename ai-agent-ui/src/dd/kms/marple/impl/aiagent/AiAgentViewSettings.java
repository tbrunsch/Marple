package dd.kms.marple.impl.aiagent;

import dd.kms.marple.api.aiagent.backend.AiAgentFactory;
import dd.kms.marple.api.aiagent.backend.AiAgentFactorySupplier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Settings shared by all AI views of one registration (see {@link AiAgentViewUIImpl#register}). The object
 * lives as long as the registered inspection settings. "Auto approve" is deliberately not persisted: It is
 * off whenever the application is started.<br>
 * <br>
 * All methods except {@link #getSystemPrompt()} must be called on the event dispatch thread.
 */
class AiAgentViewSettings
{
	private final AiAgentFactorySupplier	agentFactorySupplier;
	private final String					systemPrompt;
	/**
	 * The factory provided by the supplier. It is {@code null} as long as the supplier has not
	 * succeeded yet.
	 */
	@Nullable
	private AiAgentFactory					agentFactory;
	private boolean							autoApprove				= false;
	private final List<Runnable>			autoApproveListeners	= new ArrayList<>();

	AiAgentViewSettings(AiAgentFactorySupplier agentFactorySupplier, String systemPromptAddition) {
		this.agentFactorySupplier = agentFactorySupplier;
		this.systemPrompt = createSystemPrompt(systemPromptAddition);
	}

	private static String createSystemPrompt(@Nullable String systemPromptAddition) {
		String baseSystemPrompt = Resources.loadText("SystemPrompt.md");
		return systemPromptAddition == null || systemPromptAddition.trim().isEmpty()
			? baseSystemPrompt
			: baseSystemPrompt + "\n\n" + systemPromptAddition;
	}

	/**
	 * Returns the agent factory. The supplier is only called until it succeeds: A successfully created
	 * factory is reused, a failure is retried with the next call.
	 *
	 * @throws IOException if the supplier fails to create the factory
	 */
	AiAgentFactory getAgentFactory() throws IOException {
		if (agentFactory == null) {
			agentFactory = Objects.requireNonNull(agentFactorySupplier.get(), "The agent factory supplier returned null");
		}
		return agentFactory;
	}

	/**
	 * Returns the system prompt including the addition. In contrast to the other methods, this method may be
	 * called from any thread because the system prompt is immutable.
	 */
	String getSystemPrompt() {
		return systemPrompt;
	}

	boolean isAutoApprove() {
		return autoApprove;
	}

	void setAutoApprove(boolean autoApprove) {
		if (this.autoApprove == autoApprove) {
			return;
		}
		this.autoApprove = autoApprove;
		// iterate over a copy so listeners may add or remove listeners without causing a ConcurrentModificationException
		new ArrayList<>(autoApproveListeners).forEach(Runnable::run);
	}

	void addAutoApproveListener(Runnable listener) {
		autoApproveListeners.add(listener);
	}

	void removeAutoApproveListener(Runnable listener) {
		autoApproveListeners.remove(listener);
	}
}
